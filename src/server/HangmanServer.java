package src.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import src.common.Protocol;

public class HangmanServer {

    private final ArrayList<ClientHandler> players = new ArrayList<>();
    private HangmanState gameState;

    public static void main(String[] args) {
        new HangmanServer().start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(Protocol.PORT)) {
            gameState = new HangmanState(HangmanWords.getRandomWord());
            System.out.println("Servidor iniciado na porta " + Protocol.PORT + ". À espera de jogadores...");

            // aguarda primeiro jogador sem timeout
            Socket first = serverSocket.accept();
            players.add(new ClientHandler(first, 1));
            System.out.println("Jogador 1 entrou. A aguardar mais jogadores (" + Protocol.LOBBY_TIMEOUT_MS / 1000 + "s timeout)...");

            // depois do primeiro, timeout de lobby para os restantes
            // TODO: O timeout do lobby não é absoluto. O requisito pede que expire 20s após a entrada do 1º jogador.
            // Atualmente, cada accept() reinicia a janela de 20s. Deve ser calculado o tempo restante.
            serverSocket.setSoTimeout(Protocol.LOBBY_TIMEOUT_MS);
            try {
                while (players.size() < Protocol.MAX_PLAYERS) {
                    Socket socket = serverSocket.accept();
                    //sincronizacao p garantir que a leitura do size e adicionar o jogador seja atomico
                    //sem isto tinhamos race condition
                    synchronized (players) {
                        if (players.size() < Protocol.MAX_PLAYERS) {
                            int id = players.size() + 1;
                            players.add(new ClientHandler(socket, id));
                            System.out.println("Jogador " + id + " entrou.");
                        } else {
                            //rejeita a ligacao caso o lobby tenha enchido enquanto esperavamos
                            socket.close();
                        }
                    }
                }
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout do lobby. A iniciar com " + players.size() + " jogador(es).");
            }

            if (players.size() < Protocol.MIN_PLAYERS) {
                System.out.println("Jogadores insuficientes (mínimo: " + Protocol.MIN_PLAYERS + "). A terminar.");
                return;
            }

            // rejeita ligações tardias com FULL enquanto o jogo decorre
            serverSocket.setSoTimeout(0);
            Thread rejector = new Thread(() -> {
                while (true) {
                    try {
                        Socket late = serverSocket.accept();
                        PrintWriter pw = new PrintWriter(
                                new BufferedWriter(new OutputStreamWriter(late.getOutputStream())), true);
                        pw.println(Protocol.FULL);
                        late.close();
                        System.out.println("Ligação tardia rejeitada (FULL).");
                    } catch (IOException e) {
                        break; // serverSocket fechou — jogo terminou
                    }
                }
            });
            rejector.setDaemon(true);
            rejector.start();

            runGame();

        } catch (IOException e) {
            System.out.println("Erro no servidor: " + e.getMessage());
        }
    }

    private void runGame() {
        // envia WELCOME e START a todos
        for (ClientHandler player : players)
            player.sendWelcome(players.size());

        for (ClientHandler player : players)
            player.sendStart(gameState.getMask(), gameState.getAttemptsLeft(), Protocol.ROUND_TIMEOUT_MS);

        System.out.println("Jogo iniciado! Palavra: " + gameState.getWord());

        ExecutorService executor = Executors.newFixedThreadPool(players.size());
        int round = 0;

        while (!gameState.isFinished()) {
            round++;
            gameState.setRound(round);

            String mask        = gameState.getMask();
            int attempts       = gameState.getAttemptsLeft();
            String usedLetters = gameState.getUsedLettersString();

            System.out.println("\n-- Ronda " + round + " | " + mask + " | tentativas: " + attempts);

            // envia ROUND a todos e define timeout por ronda
            for (ClientHandler player : players) {
                player.sendRound(round, mask, attempts, usedLetters);
                player.setRoundTimeout(Protocol.ROUND_TIMEOUT_MS);
            }

            // recolhe guesses de todos em paralelo
            List<Future<?>> futures = new ArrayList<>();
            for (ClientHandler player : players) {
                futures.add(executor.submit(() -> {
                    try {
                        player.waitGuess();
                    } catch (IOException e) {
                        System.out.println("Jogador " + player.getPlayerId() + " desconectou-se.");
                    }
                }));
            }
            for (Future<?> f : futures) {
                try { f.get(); } catch (Exception ignored) {}
            }

            // processa guesses e determina vencedores
            List<Integer> winners = new ArrayList<>();
            for (ClientHandler player : players) {
                String guess = player.getCurrentGuess();

                if (guess.isEmpty()) {
                    // fix 1: timeout consome uma tentativa
                    System.out.println("Jogador " + player.getPlayerId() + " não respondeu (timeout). -1 tentativa.");
                    gameState.decrementAttempts();
                    continue;
                }

                System.out.println("Jogador " + player.getPlayerId() + " jogou: " + guess);

                // FIX: A lógica atual apenas premeia o primeiro jogador que completa a palavra. 
                // O requisito pede: "Victory: One or more players guess the full word in the same round."
                // Todos os que contribuem corretamente na ronda em que a palavra é completada devem ganhar.
                boolean wasComplete = gameState.isWordGuessed();
                gameState.processGuess(guess);

                if (!wasComplete && gameState.isWordGuessed()) {
                    winners.add(player.getPlayerId());
                }
            }

            // verifica condição de fim
            if (!winners.isEmpty()) {
                String winnerIds = winners.toString().replaceAll("[\\[\\] ]", "");
                System.out.println("Vitória! Vencedor(es): " + winnerIds + " | Palavra: " + gameState.getWord());
                for (ClientHandler player : players)
                    player.sendEndWin(winnerIds, gameState.getWord());

            } else if (gameState.getAttemptsLeft() <= 0) {
                gameState.setFinished(true);
                System.out.println("Derrota. Tentativas esgotadas. Palavra: " + gameState.getWord());
                for (ClientHandler player : players)
                    player.sendEndLose(gameState.getWord());

            } else {
                // envia estado atualizado e continua
                String newMask    = gameState.getMask();
                int newAttempts   = gameState.getAttemptsLeft();
                String newUsed    = gameState.getUsedLettersString();
                for (ClientHandler player : players)
                    player.sendState(newMask, newAttempts, newUsed);
            }
        }

        executor.shutdown();
        for (ClientHandler player : players)
            player.close();

        System.out.println("Jogo terminado.");
    }
}
