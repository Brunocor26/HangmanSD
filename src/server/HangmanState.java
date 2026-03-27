package src.server;

import java.util.ArrayList;
import java.util.Set;

public class HangmanState {
    private final String word;           //word (all caps)
    private final char[] placeholder;           //just the placeholders like: ['_', '_', '_', '_'] for a 4 letter word
    private int attemptsLeft;
    private ArrayList <Character> usedLetters;
    private int round;
    private boolean finished;

    //constructor only with the word for now
    public HangmanState(String word){
        this.word=word;
        this.placeholder=new char[word.length()];
        this.attemptsLeft=6;
        this.usedLetters= new ArrayList<Character>();
        this.round=0;
        this.finished= false;

        //set up the placeholder
        for(int i=0; i<placeholder.length; i++)
            placeholder[i]= '_';
    }

    //getters & setters
    public synchronized String getWord() {
        return word;
    }

    public synchronized char[] getPlaceholder() {
        return placeholder;
    }

    public synchronized int getAttemptsLeft() {
        return attemptsLeft;
    }

    public synchronized void setAttemptsLeft(int attemptsLeft) {
        this.attemptsLeft = attemptsLeft;
    }

    public synchronized ArrayList<Character> getUsedLetters() {
        return usedLetters;
    }

    public synchronized void setUsedLetters(ArrayList<Character> usedLetters) {
        this.usedLetters = usedLetters;
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
