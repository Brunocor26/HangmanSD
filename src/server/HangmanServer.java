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
            // Prazo absoluto para o lobby (20s após o primeiro jogador entrar)
            long lobbyEndTime = System.currentTimeMillis() + Protocol.LOBBY_TIMEOUT_MS;

            while (players.size() < Protocol.MAX_PLAYERS) {
                long timeLeftMs = lobbyEndTime - System.currentTimeMillis();
                if (timeLeftMs <= 0) break;

                // Se faltarem mais de 5s, esperamos até chegar à marca dos 5s
                // Se faltarem 5s ou menos, usamos timeout de 1s para fazer a contagem no ecrã
                int nextTimeout = (timeLeftMs > 5000) ? (int)(timeLeftMs - 5000) : 1000;

                try {
                    serverSocket.setSoTimeout(nextTimeout);
                    Socket socket = serverSocket.accept();
                    synchronized (players) {
                        if (players.size() < Protocol.MAX_PLAYERS) {
                            int id = players.size() + 1;
                            players.add(new ClientHandler(socket, id));
                            System.out.println("Jogador " + id + " entrou.");
                        } else {
                            socket.close();
                        }
                    }
                } catch (SocketTimeoutException e) {
                    // Acontece quando o timeout de 'nextTimeout' expira
                    long remaining = lobbyEndTime - System.currentTimeMillis();
                    if (remaining <= 5000 && remaining > 0) {
                        System.out.println("Lobby fecha em " + (remaining / 1000 + 1) + " segundos...");
                    }
                } catch (IOException e) {
                    System.out.println("Erro ao aceitar jogador: " + e.getMessage());
                    break;
                }
            }

            System.out.println("Lobby fechado. Jogadores totais: " + players.size());

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


        int round = 0;
        List<ClientHandler> activePlayers = new ArrayList<>(players);

        while (!gameState.isFinished()) {
            round++;
            gameState.setRound(round);

            if (activePlayers.size() < Protocol.MIN_PLAYERS) {
                System.out.println("Jogadores insuficientes para continuar (mínimo: " + Protocol.MIN_PLAYERS + ").");
                for (ClientHandler p : activePlayers) {
                    p.sendEndLose(gameState.getWord());
                }
                break;
            }

            String mask        = gameState.getMask();
            int attempts       = gameState.getAttemptsLeft();
            String usedLetters = gameState.getUsedLettersString();

            System.out.println("\n-- Ronda " + round + " | " + mask + " | tentativas: " + attempts);

            // envia ROUND a todos e define timeout por ronda
            for (ClientHandler player : activePlayers) {
                player.sendRound(round, mask, attempts, usedLetters);
                player.setRoundTimeout(Protocol.ROUND_TIMEOUT_MS);
            }

            // recolhe guesses de todos em paralelo usando a API nativa de Threads
            List<Thread> threads = new ArrayList<>();
            List<ClientHandler> disconnectedThisRound = new ArrayList<>();
            for (ClientHandler player : activePlayers) {
                Thread t = new Thread(() -> {
                    try {
                        player.waitGuess();
                    } catch (IOException e) {
                        System.out.println("Jogador " + player.getPlayerId() + " desconectou-se.");
                        synchronized (disconnectedThisRound) {
                            disconnectedThisRound.add(player);
                        }
                    }
                });
                threads.add(t);
                t.start();
            }
            // Aguarda por todas as threads desta ronda terminarem (barreira de sincronização simples)
            for (Thread t : threads) {
                try { t.join(); } catch (InterruptedException ignored) {}
            }

            // Remove os jogadores que se desconectaram nesta ronda
            activePlayers.removeAll(disconnectedThisRound);

            // processa guesses e determina vencedores
            List<Integer> winners = new ArrayList<>();
            List<String> validContributions = new ArrayList<>();

            for (ClientHandler player : activePlayers) {
                String guess = player.getCurrentGuess();

                if (guess.isEmpty()) {
                    System.out.println("Jogador " + player.getPlayerId() + " não respondeu (timeout). -1 tentativa.");
                    gameState.decrementAttempts();
                    continue;
                }

                guess = guess.toUpperCase();
                System.out.println("Jogador " + player.getPlayerId() + " jogou: " + guess);

                // Se o jogo já acabou por uma jogada anterior nesta ronda,
                // apenas verificamos se este jogador também contribuiu para a vitória.
                if (guess.equals(gameState.getWord())) {
                    if (!winners.contains(player.getPlayerId())) winners.add(player.getPlayerId());
                    gameState.processGuess(guess);
                    gameState.setFinished(true);
                } else if (guess.length() == 1) {
                    String currentUsed = gameState.getUsedLettersString().replaceAll(" ", "");
                    boolean wasUsedBeforeRound = currentUsed.contains(guess);
                    
                    // Se a letra for correta e não tiver sido usada em rondas ANTERIORES
                    if (gameState.getWord().contains(guess) && !wasUsedBeforeRound) {
                        if (!winners.contains(player.getPlayerId())) winners.add(player.getPlayerId());
                        validContributions.add(guess);
                        gameState.processGuess(guess);
                    } else if (gameState.getWord().contains(guess) && validContributions.contains(guess)) {
                        // Se outro jogador já jogou esta letra NESTA ronda
                        if (!winners.contains(player.getPlayerId())) winners.add(player.getPlayerId());
                    } else {
                        // Letra errada ou já usada em rondas anteriores
                        gameState.processGuess(guess);
                    }
                } else {
                    // Palavra errada
                    gameState.processGuess(guess);
                }
            }

            // Após processar todos os jogadores da ronda, verificamos se a palavra foi completada
            if (gameState.isWordGuessed()) {
                gameState.setFinished(true);
            }

            // Se o jogo acabou, enviamos as mensagens finais e saímos do loop
            if (gameState.isFinished()) {
                if (!winners.isEmpty()) {
                    String winnerIds = winners.toString().replaceAll("[\\[\\] ]", "");
                    System.out.println("Vitória! Vencedor(es): " + winnerIds + " | Palavra: " + gameState.getWord());
                    for (ClientHandler player : activePlayers)
                        player.sendEndWin(winnerIds, gameState.getWord());
                } else if (gameState.getAttemptsLeft() <= 0) {
                    System.out.println("Derrota. Tentativas esgotadas. Palavra: " + gameState.getWord());
                    for (ClientHandler player : activePlayers)
                        player.sendEndLose(gameState.getWord());
                } else {
                    // Caso raro: palavra completa mas sem vencedores registados nesta ronda 
                    // (ex: completada por erro de lógica ou timeout que não deveria acontecer)
                    System.out.println("Fim de jogo. Palavra adivinhada: " + gameState.getWord());
                    for (ClientHandler player : activePlayers)
                        player.sendEndWin("Todos", gameState.getWord());
                }
                break; // SAI DO LOOP PRINCIPAL DO JOGO
            } else if (gameState.getAttemptsLeft() <= 0) {
                // Caso as tentativas acabem mas isFinished ainda seja falso
                gameState.setFinished(true);
                System.out.println("Derrota. Tentativas esgotadas. Palavra: " + gameState.getWord());
                for (ClientHandler player : activePlayers)
                    player.sendEndLose(gameState.getWord());
                break;
            } else {
                // O jogo continua, envia o estado atualizado
                String newMask    = gameState.getMask();
                int newAttempts   = gameState.getAttemptsLeft();
                String newUsed    = gameState.getUsedLettersString();
                for (ClientHandler player : activePlayers)
                    player.sendState(newMask, newAttempts, newUsed);
            }
        }


        for (ClientHandler player : players)
            player.close();

        System.out.println("Jogo terminado.");
    }

    public final static void clearConsole()
{
    try
    {
        final String os = System.getProperty("os.name");
        
        if (os.contains("Windows"))
        {
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "cls"});
        }
        else //linux e mac(?)
        {
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "clear"});
        }
    }
    catch (final Exception e)
    {
        //  Handle any exceptions.
    }
}
}
