package src.server;

import java.util.ArrayList;
import src.common.Protocol;

public class HangmanState {
    private final String word;
    private final char[] placeholder;
    private int attemptsLeft;
    private ArrayList<Character> usedLetters;
    private int round;
    private boolean finished;

    public HangmanState(String word) {
        this.word = word.toUpperCase();  //to secure that caps dont matter
        this.placeholder = new char[word.length()];
        this.attemptsLeft = Protocol.MAX_ATTEMPTS;
        this.usedLetters = new ArrayList<>();
        this.round = 0;
        this.finished = false;

        for (int i = 0; i < placeholder.length; i++)
            placeholder[i] = '_';
    }

    //game logic

    public synchronized void processGuess(String guess) {
        if (guess == null || guess.isEmpty() || finished) return;
        guess = guess.toUpperCase();

        if (guess.length() == 1) {
            char letter = guess.charAt(0);
            if (usedLetters.contains(letter)) return; //already used, no punishment 
            usedLetters.add(letter);

            boolean found = false;
            for (int i = 0; i < word.length(); i++) {
                if (word.charAt(i) == letter) {
                    placeholder[i] = letter;
                    found = true;
                }
            }
            if (!found) attemptsLeft--;

        } else {
            //guess is GOOD, reveal the word
            if (guess.equals(word)) {
                for (int i = 0; i < word.length(); i++)
                    placeholder[i] = word.charAt(i);
            } else { //wrong guess
                attemptsLeft--;
            }
        }
    }

    //if there is any placeholder, word isnt guessed yet
    public synchronized boolean isWordGuessed() {
        for (char c : placeholder)
            if (c == '_') return false;
        return true;
    }

    //get the mask/placeholder
    public synchronized String getMask() {
        return new String(placeholder);  //makes a string out of a char array
    }

    
    public synchronized String getUsedLettersString() {
        StringBuilder sb = new StringBuilder();
        for (Character c : usedLetters) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(c);
        }
        return sb.toString();
    }

    //getters & setters

    public synchronized String getWord() {
        return word;
    }

    public synchronized int getAttemptsLeft() {
        return attemptsLeft;
    }

    public synchronized void decrementAttempts() {
        attemptsLeft--;
    }

    public synchronized int getRound() {
        return round;
    }

    public synchronized void setRound(int round) {
        this.round = round;
    }

    public synchronized boolean isFinished() {
        return finished;
    }

    public synchronized void setFinished(boolean finished) {
        this.finished = finished;
    }
}
