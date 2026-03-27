package src.server;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class HangmanServer {
    private static final int PORT = 11111;
    private static final int MAX_PLAYERS = 4;
    private static final int TIMEOUT_MS = 20000; //20 seconds timeout

    //list to store players
    private ArrayList<ClientHandler> players = new ArrayList<>();
    private static HangmanState game_state;

    private boolean game_started = false;


    public static void main(String args[]) throws IOException {

        try{
        game_state = new HangmanState(HangmanWords.getRandomWord());
        //open serverSOcket
        ServerSocket server= new ServerSocket(PORT);
        System.out.println("Server running, waiting for players to connect.");

        } catch(IOException e){
            System.out.println("Error: " + e.getMessage());
        }
        

        //inicializar gamestate com a palavra aleatoria escolhida


    }
}