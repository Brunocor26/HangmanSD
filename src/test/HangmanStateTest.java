package src.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.common.Protocol;
import src.server.HangmanState;

import static org.junit.jupiter.api.Assertions.*;

public class HangmanStateTest {

    private HangmanState state;

    @BeforeEach
    void setUp() {
        state = new HangmanState("BRASIL"); // B-R-A-S-I-L
    }

    // Estado inicial

    @Test
    void initialMaskAllUnderscores() {
        assertEquals("______", state.getMask());
    }

    @Test
    void initialAttemptsLeft() {
        assertEquals(Protocol.MAX_ATTEMPTS, state.getAttemptsLeft());
    }

    @Test
    void initialNotFinished() {
        assertFalse(state.isFinished());
    }

    @Test
    void initialWordNotGuessed() {
        assertFalse(state.isWordGuessed());
    }

    @Test
    void initialUsedLettersEmpty() {
        assertEquals("", state.getUsedLettersString());
    }

    @Test
    void getMaskLengthMatchesWord() {
        assertEquals("BRASIL".length(), state.getMask().length());
    }

    // Letra correta

    @Test
    void correctLetterRevealedInMask() {
        state.processGuess("B");
        assertEquals("B_____", state.getMask());
    }

    @Test
    void correctLetterNoAttemptPenalty() {
        int before = state.getAttemptsLeft();
        state.processGuess("B");
        assertEquals(before, state.getAttemptsLeft());
    }

    @Test
    void correctLetterRepeatedInWord() {
        HangmanState banana = new HangmanState("BANANA");
        banana.processGuess("A");
        assertEquals("_A_A_A", banana.getMask());
    }

    @Test
    void correctLetterAddedToUsedLetters() {
        state.processGuess("B");
        assertTrue(state.getUsedLettersString().contains("B"));
    }

    // Letra errada

    @Test
    void wrongLetterDecrementsAttempts() {
        state.processGuess("Z");
        assertEquals(Protocol.MAX_ATTEMPTS - 1, state.getAttemptsLeft());
    }

    @Test
    void wrongLetterMaskUnchanged() {
        state.processGuess("Z");
        assertEquals("______", state.getMask());
    }

    @Test
    void wrongLetterAddedToUsedLetters() {
        state.processGuess("Z");
        assertTrue(state.getUsedLettersString().contains("Z"));
    }

    // Letra já usada

    @Test
    void alreadyUsedLetterNoPenalty() {
        state.processGuess("B");
        int attempts = state.getAttemptsLeft();
        state.processGuess("B");
        assertEquals(attempts, state.getAttemptsLeft());
    }

    @Test
    void alreadyUsedLetterNotDuplicated() {
        state.processGuess("B");
        state.processGuess("B");
        assertEquals("B", state.getUsedLettersString());
    }

    // Input em minúsculas

    @Test
    void lowercaseGuessHandled() {
        state.processGuess("b");
        assertEquals("B_____", state.getMask());
    }

    @Test
    void lowercaseWrongLetterDecrementsAttempts() {
        state.processGuess("z");
        assertEquals(Protocol.MAX_ATTEMPTS - 1, state.getAttemptsLeft());
    }

    // Adivinhar palavra completa

    @Test
    void correctWordFillsMask() {
        state.processGuess("BRASIL");
        assertEquals("BRASIL", state.getMask());
    }

    @Test
    void correctWordIsGuessed() {
        state.processGuess("BRASIL");
        assertTrue(state.isWordGuessed());
    }

    @Test
    void correctWordLowercaseWorks() {
        state.processGuess("brasil");
        assertTrue(state.isWordGuessed());
    }

    @Test
    void wrongWordDecrementsAttempts() {
        state.processGuess("FRANCA");
        assertEquals(Protocol.MAX_ATTEMPTS - 1, state.getAttemptsLeft());
    }

    @Test
    void wrongWordMaskUnchanged() {
        state.processGuess("FRANCA");
        assertEquals("______", state.getMask());
    }

    // Vitória por letras

    @Test
    void wordCompleteAfterAllLetters() {
        state.processGuess("B");
        state.processGuess("R");
        state.processGuess("A");
        state.processGuess("S");
        state.processGuess("I");
        state.processGuess("L");
        assertTrue(state.isWordGuessed());
        assertEquals("BRASIL", state.getMask());
    }

    // Derrota por tentativas

    @Test
    void attemptsReachZeroAfterSixWrongGuesses() {
        state.processGuess("Z");
        state.processGuess("X");
        state.processGuess("Q");
        state.processGuess("W");
        state.processGuess("Y");
        state.processGuess("K");
        assertEquals(0, state.getAttemptsLeft());
    }

    // Sem ação após jogo terminado

    @Test
    void noProcessingAfterFinished() {
        state.setFinished(true);
        state.processGuess("B");
        assertEquals("______", state.getMask());
        assertEquals(Protocol.MAX_ATTEMPTS, state.getAttemptsLeft());
    }

    // getUsedLettersString formato

    @Test
    void usedLettersStringSeparatedBySpace() {
        state.processGuess("B");
        state.processGuess("R");
        assertEquals("B R", state.getUsedLettersString());
    }

    // Palavra de uma só letra

    @Test
    void singleLetterWord() {
        HangmanState s = new HangmanState("A");
        s.processGuess("A");
        assertTrue(s.isWordGuessed());
        assertEquals("A", s.getMask());
    }

    // Null/empty guess não lança exceção

    @Test
    void nullGuessIgnored() {
        assertDoesNotThrow(() -> state.processGuess(null));
        assertEquals("______", state.getMask());
    }

    @Test
    void emptyGuessIgnored() {
        assertDoesNotThrow(() -> state.processGuess(""));
        assertEquals("______", state.getMask());
    }
}
