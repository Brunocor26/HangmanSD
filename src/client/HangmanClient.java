package src.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import src.common.Protocol;

public class HangmanClient {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Scanner scanner = new Scanner(System.in);

    private int playerId;
    private int totalPlayers;
    private int roundTimeoutMs;

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : Protocol.DEFAULT_HOST;
        new HangmanClient().start(host);
    }

    public void start(String host) {
        try {
            socket = new Socket(host, Protocol.PORT);
            out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Ligado ao servidor em " + host + ":" + Protocol.PORT);

            String line;
            while ((line = in.readLine()) != null) {
                handleMessage(line);
            }

        } catch (ConnectException e) {
            System.out.println("Não foi possível ligar ao servidor. Verifique se está a correr.");
        } catch (IOException e) {
            System.out.println("Ligação perdida: " + e.getMessage());
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (IOException ignored) {}
        }
    }

    // Protocol handler

    private void handleMessage(String message) throws IOException {
        if (message.startsWith(Protocol.WELCOME)) {
            handleWelcome(message);

        } else if (message.startsWith(Protocol.START)) {
            handleStart(message);

        } else if (message.startsWith(Protocol.ROUND)) {
            handleRound(message);

        } else if (message.startsWith(Protocol.STATE)) {
            handleState(message);

        } else if (message.startsWith(Protocol.END_WIN)) {
            handleEndWin(message);

        } else if (message.startsWith(Protocol.END_LOSE)) {
            handleEndLose(message);

        } else if (message.equals(Protocol.FULL)) {
            System.out.println("O servidor está cheio. Tente novamente mais tarde.");
        }
    }

    // WELCOME <id> <totalPlayers>
    private void handleWelcome(String message) {
        String[] parts = message.split(" ");
        playerId = Integer.parseInt(parts[1]);
        totalPlayers = Integer.parseInt(parts[2]);

        System.out.println("\n╔══════════════════════════════╗");
        System.out.println("║      BEM-VINDO AO HANGMAN    ║");
        System.out.println("╚══════════════════════════════╝");
        System.out.printf("  És o Jogador %d  |  Total: %d jogadores%n", playerId, totalPlayers);
        System.out.println("  A aguardar início do jogo...");
    }

    // START <mask> <attempts> <roundTimeoutMs>
    private void handleStart(String message) {
        String[] parts = message.split(" ");
        String mask = parts[1];
        int attempts = Integer.parseInt(parts[2]);
        roundTimeoutMs = Integer.parseInt(parts[3]);

        System.out.println("\n══════════════ JOGO INICIADO ══════════════");
        System.out.printf("  Tempo por ronda: %d segundos%n", roundTimeoutMs / 1000);
        printState(mask, attempts, "");
    }

    // ROUND <k> <mask> <attempts> <usedLetters>
    private void handleRound(String message) throws IOException {
        String[] parts = message.split(" ", 5);
        int round = Integer.parseInt(parts[1]);
        String mask = parts[2];
        int attempts = Integer.parseInt(parts[3]);
        String usedLetters = parts.length > 4 ? parts[4] : "";

        System.out.printf("%n════════════════ Ronda %d ════════════════%n", round);
        printState(mask, attempts, usedLetters);

        sendGuess();
    }

    // STATE <mask> <attempts> <usedLetters>
    private void handleState(String message) {
        String[] parts = message.split(" ", 4);
        String mask = parts[1];
        int attempts = Integer.parseInt(parts[2]);
        String usedLetters = parts.length > 3 ? parts[3] : "";

        System.out.println("\n  [Atualização do estado]");
        printState(mask, attempts, usedLetters);
    }

    // END WIN <winnerIds> <word>
    private void handleEndWin(String message) {
        String[] parts = message.split(" ", 4);
        String winnerIds = parts[2];
        String word = parts[3];

        System.out.println("\n╔══════════════════════════════╗");
        System.out.println("║         FIM DO JOGO          ║");
        System.out.println("╚══════════════════════════════╝");
        System.out.println("  A palavra era: " + word);

        if (winnerIds.contains(String.valueOf(playerId))) {
            System.out.println("  PARABÉNS! Você ganhou! \uD83C\uDF89");
        } else {
            System.out.println("  O(s) jogador(es) " + winnerIds + " ganhou/ganharam.");
            System.out.println("  Melhor sorte na próxima!");
        }
    }

    // END LOSE <word>
    private void handleEndLose(String message) {
        String[] parts = message.split(" ", 3);
        String word = parts[2];

        System.out.println("\n╔══════════════════════════════╗");
        System.out.println("║         FIM DO JOGO          ║");
        System.out.println("╚══════════════════════════════╝");
        System.out.println("  Ninguém adivinhou a palavra.");
        System.out.println("  A palavra era: " + word);
        printHangman(0);
    }

    // Display helpers

    private void printState(String mask, int attempts, String usedLetters) {
        System.out.println();
        printHangman(attempts);
        System.out.println();

        // Space out each character: B _ A _ _
        StringBuilder display = new StringBuilder("  Palavra: ");
        for (char c : mask.toCharArray()) {
            display.append(c).append(' ');
        }
        System.out.println(display.toString().trim());
        System.out.println("  Tentativas restantes: " + attempts);

        if (usedLetters != null && !usedLetters.isEmpty()) {
            System.out.println("  Letras já usadas: " + usedLetters);
        }
    }

    private void printHangman(int attemptsLeft) {
        String[] stages = {
            // 6 — nenhum erro
            "  +---+\n  |   |\n      |\n      |\n      |\n      |\n=========",
            // 5
            "  +---+\n  |   |\n  O   |\n      |\n      |\n      |\n=========",
            // 4
            "  +---+\n  |   |\n  O   |\n  |   |\n      |\n      |\n=========",
            // 3
            "  +---+\n  |   |\n  O   |\n /|   |\n      |\n      |\n=========",
            // 2
            "  +---+\n  |   |\n  O   |\n /|\\  |\n      |\n      |\n=========",
            // 1
            "  +---+\n  |   |\n  O   |\n /|\\  |\n /    |\n      |\n=========",
            // 0 — morto
            "  +---+\n  |   |\n  O   |\n /|\\  |\n / \\  |\n      |\n========="
        };

        int index = 6 - attemptsLeft;
        index = Math.max(0, Math.min(index, stages.length - 1));
        System.out.println(stages[index]);
    }

    // Input

    private void sendGuess() {
        System.out.print("\n  A sua jogada (letra ou palavra): ");
        String guess = scanner.nextLine().trim().toUpperCase();
        out.println(Protocol.guess(guess));
    }
}
