package src.server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class HangmanServer {
    private static final int PORT = 11111;
    private static final int MAX_PLAYERS = 4;
    private static final int TIMEOUT_MS = 20000; //20 seconds timeout

    //list to store players
    private ArrayList<ClientHandler> players = new ArrayList<>();
    private static HangmanState game_state;

    private volatile boolean game_started = false;


    public static void main(String args[]) throws IOException {
        new HangmanServer().start();
    }

    public void start() {
        try {
            game_state = new HangmanState(HangmanWords.getRandomWord());
            ServerSocket server = new ServerSocket(PORT);
            System.out.println("Servidor a funcionar, à espera de jogadores.");

            while (players.size() < MAX_PLAYERS) {
                Socket socket = server.accept();
                int playerId = players.size() + 1;
                ClientHandler handler = new ClientHandler(socket, playerId, game_state);
                players.add(handler);
                System.out.println("Jogador " + playerId + " entrou! Bem vindo.");
            }

            game_started = true;

            for (ClientHandler player : players) {
                player.sendWelcome(players.size());
                new Thread(player).start();
            }

            
        } catch (IOException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}