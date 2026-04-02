package src.test;

import org.junit.jupiter.api.Test;
import src.common.Protocol;

import static org.junit.jupiter.api.Assertions.*;

public class ProtocolTest {

    // Formato das mensagens

    @Test
    void welcomeFormat() {
        assertEquals("WELCOME 2 4", Protocol.welcome(2, 4));
    }

    @Test
    void startFormat() {
        assertEquals("START ______ 6 30000", Protocol.start("______", 6, 30000));
    }

    @Test
    void roundFormat() {
        assertEquals("ROUND 3 _A___ 5 A E", Protocol.round(3, "_A___", 5, "A E"));
    }

    @Test
    void stateFormat() {
        assertEquals("STATE _A___ 4 A E I", Protocol.state("_A___", 4, "A E I"));
    }

    @Test
    void endWinFormat() {
        assertEquals("END WIN 1,3 BRASIL", Protocol.endWin("1,3", "BRASIL"));
    }

    @Test
    void endLoseFormat() {
        assertEquals("END LOSE BRASIL", Protocol.endLose("BRASIL"));
    }

    @Test
    void guessFormat() {
        assertEquals("GUESS A", Protocol.guess("A"));
    }

    @Test
    void guessWordFormat() {
        assertEquals("GUESS BRASIL", Protocol.guess("BRASIL"));
    }

    // Prefixos corretos

    @Test
    void welcomeStartsWithConstant() {
        assertTrue(Protocol.welcome(1, 2).startsWith(Protocol.WELCOME));
    }

    @Test
    void startStartsWithConstant() {
        assertTrue(Protocol.start("__", 6, 30000).startsWith(Protocol.START));
    }

    @Test
    void roundStartsWithConstant() {
        assertTrue(Protocol.round(1, "__", 6, "").startsWith(Protocol.ROUND));
    }

    @Test
    void stateStartsWithConstant() {
        assertTrue(Protocol.state("__", 6, "").startsWith(Protocol.STATE));
    }

    @Test
    void endWinStartsWithConstant() {
        assertTrue(Protocol.endWin("1", "WORD").startsWith(Protocol.END_WIN));
    }

    @Test
    void endLoseStartsWithConstant() {
        assertTrue(Protocol.endLose("WORD").startsWith(Protocol.END_LOSE));
    }

    @Test
    void guessStartsWithConstant() {
        assertTrue(Protocol.guess("A").startsWith(Protocol.GUESS));
    }

    // Constantes de configuração

    @Test
    void port() {
        assertEquals(11111, Protocol.PORT);
    }

    @Test
    void maxPlayers() {
        assertEquals(4, Protocol.MAX_PLAYERS);
    }

    @Test
    void minPlayers() {
        assertEquals(2, Protocol.MIN_PLAYERS);
    }

    @Test
    void maxAttempts() {
        assertEquals(6, Protocol.MAX_ATTEMPTS);
    }

    @Test
    void minPlayersLessThanMax() {
        assertTrue(Protocol.MIN_PLAYERS < Protocol.MAX_PLAYERS);
    }

    @Test
    void timeoutsPositive() {
        assertTrue(Protocol.LOBBY_TIMEOUT_MS > 0);
        assertTrue(Protocol.ROUND_TIMEOUT_MS > 0);
    }
}
