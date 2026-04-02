package src.server;

import java.io.IOException;
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
            System.out.println("Jogador 1 entrou. Aguardando mais jogadores (" + Protocol.LOBBY_TIMEOUT_MS / 1000 + "s timeout)...");

            // depois do primeiro, timeout de lobby para os restantes
            serverSocket.setSoTimeout(Protocol.LOBBY_TIMEOUT_MS);
            try {
                while (players.size() < Protocol.MAX_PLAYERS) {
                    Socket socket = serverSocket.accept();
                    int id = players.size() + 1;
                    players.add(new ClientHandler(socket, id));
                    System.out.println("Jogador " + id + " entrou.");
                }
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout do lobby. A iniciar com " + players.size() + " jogador(es).");
            }

            if (players.size() < Protocol.MIN_PLAYERS) {
                System.out.println("Jogadores insuficientes (mínimo: " + Protocol.MIN_PLAYERS + "). A terminar.");
                return;
            }

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
                        System.out.println("Jogador " + player.getPlayerId() + " desconectou.");
                    }
                }));
            }
            for (Future<?> f : futures) {
                try { f.get(); } catch (Exception ignored) {}
            }

            // processa guesses e determina vencedores
            List<Integer> winners = new ArrayList<>();
            for (ClientHandler player : players) {
                if (gameState.isFinished()) break;

                String guess = player.getCurrentGuess();
                if (guess.isEmpty()) {
                    System.out.println("Jogador " + player.getPlayerId() + " não respondeu (timeout).");
                    continue;
                }

                System.out.println("Jogador " + player.getPlayerId() + " jogou: " + guess);
                gameState.processGuess(guess);

                if (gameState.isWordGuessed()) {
                    winners.add(player.getPlayerId());
                    gameState.setFinished(true);
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
