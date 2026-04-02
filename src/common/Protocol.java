package src.common;

public class Protocol {

    // Rede
    public static final int    PORT        = 11111;
    public static final String DEFAULT_HOST = "localhost";

    // Parâmetros de jogo
    public static final int MAX_PLAYERS   = 4;
    public static final int MIN_PLAYERS   = 2;
    public static final int MAX_ATTEMPTS  = 6;
    public static final int LOBBY_TIMEOUT_MS = 20_000; // espera máxima no lobby
    public static final int ROUND_TIMEOUT_MS = 30_000; // tempo por ronda

    // Mensagens Servidor -> Cliente
    public static final String WELCOME  = "WELCOME";   // WELCOME <id> <totalPlayers>
    public static final String START    = "START";     // START <mask> <attempts> <roundTimeoutMs>
    public static final String ROUND    = "ROUND";     // ROUND <k> <mask> <attempts> <usedLetters>
    public static final String STATE    = "STATE";     // STATE <mask> <attempts> <usedLetters>
    public static final String END_WIN  = "END WIN";   // END WIN <winnerIds> <word>
    public static final String END_LOSE = "END LOSE";  // END LOSE <word>
    public static final String FULL     = "FULL";      // FULL

    // Mensagens Cliente -> Servidor
    public static final String GUESS = "GUESS";        // GUESS <letra|palavra>

    // Construtores de mensagens (Servidor -> Cliente)
    public static String welcome(int playerId, int totalPlayers) {
        return WELCOME + " " + playerId + " " + totalPlayers;
    }

    public static String start(String mask, int attempts, int roundTimeoutMs) {
        return START + " " + mask + " " + attempts + " " + roundTimeoutMs;
    }

    public static String round(int k, String mask, int attempts, String usedLetters) {
        return ROUND + " " + k + " " + mask + " " + attempts + " " + usedLetters;
    }

    public static String state(String mask, int attempts, String usedLetters) {
        return STATE + " " + mask + " " + attempts + " " + usedLetters;
    }

    public static String endWin(String winnerIds, String word) {
        return END_WIN + " " + winnerIds + " " + word;
    }

    public static String endLose(String word) {
        return END_LOSE + " " + word;
    }

    // Construtor de mensagens (Cliente -> Servidor)
    public static String guess(String text) {
        return GUESS + " " + text;
    }
}
