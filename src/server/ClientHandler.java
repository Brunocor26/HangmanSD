package src.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

//class thats used in a thread and represents a user in the game
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final int playerId;
    private final HangmanState gameState;

    private PrintWriter out;
    private BufferedReader in;

    private String currentGuess = "";      //current play
    private boolean guessReceived = false; //to synchoronize

    public ClientHandler(Socket socket, int playerId, HangmanState gameState) {
        this.socket = socket;
        this.playerId = playerId;
        this.gameState = gameState;
    }

    @Override
    public void run() {
        //initialize the buff
        try{
            out= new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream())
            ), true);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (!gameState.isFinished()){
                waitGuess();
        }
        }catch(IOException e){
            System.out.println("Jogador " + playerId + " desconectou: " + e.getMessage());
        } finally {
            close();
        }
        
    }


    //------------------------messages (set by the defined protocol)-------------------------------------------
    public void sendWelcome(int totalPlayers) {
        send("WELCOME " + playerId + " " + totalPlayers);
    }

    public void sendStart(String mask, int attempts, int roundTimeoutMs) {
        send("START " + mask + " " + attempts + " " + roundTimeoutMs);
    }

    public void sendRound(int round, String mask, int attempts, String usedLetters) {
        send("ROUND " + round + " " + mask + " " + attempts + " " + usedLetters);
    }

    public void sendState(String mask, int attempts, String usedLetters) {
        send("STATE " + mask + " " + attempts + " " + usedLetters);
    }

    public void sendEndWin(String winnerIds, String word) {
        send("END WIN " + winnerIds + " " + word);
    }

    public void sendEndLose(String word) {
        send("END LOSE " + word);
    }

    public void sendFull() {
        send("FULL");
    }

    private void send(String message) {
        if (out != null && !socket.isClosed()) {
            out.println(message);
        }
    }

    //-------------------------------------------------------receiving the client response------------------------------


    /**
     * called by HangmanServer at the beginning of each round.
     * sets the timeout and awaits client GUESS.
     * if timeout expires, GUESS "".
     */
    public synchronized void waitGuess() throws IOException {
        guessReceived = false;
        currentGuess = "";
        try {
            String line = in.readLine();
            if (line != null && line.startsWith("GUESS ")) {
                currentGuess = line.substring(6).trim();
            }
        } catch (SocketTimeoutException e) {
            // timeout expired — currentGuess stays ""
        } finally {
            guessReceived = true;
            notifyAll();
        }
    }

    /**
     * sets round timeout via socket SO_TIMEOUT so readLine() unblocks after ms.
     */
    public void setRoundTimeout(int ms) {
        try {
            socket.setSoTimeout(ms);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Erro ao fechar socket do jogador " + playerId);
        }
    }
}
