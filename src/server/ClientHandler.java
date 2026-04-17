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
import src.common.Protocol;

// represents one player's connection; used by HangmanServer to send/receive messages

//we use the thread-per-connection architecture
public class ClientHandler {

    private final Socket socket;
    private final int playerId;

    private final PrintWriter out;
    private final BufferedReader in;

    private String currentGuess = "";      // current play
    private boolean guessReceived = false; // synchronization flag

    public ClientHandler(Socket socket, int playerId) throws IOException {
        this.socket = socket;
        this.playerId = playerId;
        this.out = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream())), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    // getters

    public int getPlayerId() {
        return playerId;
    }

    public String getCurrentGuess() {
        return currentGuess;
    }

    // messages (server -> client)
    public void sendWelcome(int totalPlayers) {
        send(Protocol.welcome(playerId, totalPlayers));
    }

    public void sendStart(String mask, int attempts, int roundTimeoutMs) {
        send(Protocol.start(mask, attempts, roundTimeoutMs));
    }

    public void sendRound(int round, String mask, int attempts, String usedLetters) {
        send(Protocol.round(round, mask, attempts, usedLetters));
    }

    public void sendState(String mask, int attempts, String usedLetters) {
        send(Protocol.state(mask, attempts, usedLetters));
    }

    public void sendEndWin(String winnerIds, String word) {
        send(Protocol.endWin(winnerIds, word));
    }

    public void sendEndLose(String word) {
        send(Protocol.endLose(word));
    }

    public void sendFull() {
        send(Protocol.FULL);
    }

    private void send(String message) {
        if (out != null && !socket.isClosed()) {
            out.println(message);
        }
    }

    // receiving the client response (client -> server)


    /**
     * called by HangmanServer at the beginning of each round.
     * sets the timeout and awaits client GUESS.
     * if timeout expires, GUESS "".
     * 
     * has synchronized to avoid race conditions and to assure only one client is changing the variable currentGuess
     */
    public synchronized void waitGuess() throws IOException {
        guessReceived = false;
        currentGuess = "";
        try {
            String line = in.readLine();
            if (line == null) {
                throw new IOException("Client disconnected");
            }
            if (line.startsWith(Protocol.GUESS + " ")) {
                currentGuess = line.substring(Protocol.GUESS.length() + 1).trim();
            }
        } catch (SocketTimeoutException e) {
            // timeout expired -> currentGuess stays ""
        } finally {
            guessReceived = true;
            notifyAll();
        }
    }

    /**
     * sets round timeout via socket SO_TIMEOUT- readLine() unblocks after ms.
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
