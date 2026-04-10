package src.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.common.Protocol;
import src.server.HangmanState;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HangmanStateConcurrencyTest {

    private HangmanState state;

    @BeforeEach
    void setUp() {
        state = new HangmanState("PROGRAMMING");
    }

    @Test
    void testConcurrentWrongGuesses() throws InterruptedException {
        // We have MAX_ATTEMPTS. Let's make 100 threads guess wrong letters.
        // It should properly hit 0 and not go below 0 if decrement logic in processGuess handles it (it doesn't explicitly bound it to 0 in HangmanState but it shouldn't skip decrements).
        // Actually, HangmanState doesn't cap at zero, so if we decrement 10 times, it will be MAX_ATTEMPTS - 10.
        // The goal of concurrency test here is to assure no lost updates to attemptsLeft!
        
        int nThreads = 100;
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(nThreads);

        for (int i = 0; i < nThreads; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    latch.await();
                    // All threads try to process a distinct wrong guess to avoid the "already guessed" bypass
                    // We just use a different non-alphabetic char for each to ensure it's not the correct letter and not repeated
                    char c = (char) ('A' + index + 50); 
                    state.processGuess(String.valueOf(c));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        latch.countDown();
        doneLatch.await();
        executor.shutdown();

        // 100 wrong guesses should result in AttemptsLeft decreasing by exactly 100
        // Provided MAX_ATTEMPTS is 6, it will just drop to 6 - 100 = -94.
        // Main verification: exactly 100 decrements occurred (no lost updates)
        assertEquals(Protocol.MAX_ATTEMPTS - nThreads, state.getAttemptsLeft());
    }

    @Test
    void testConcurrentCorrectGuesses() throws InterruptedException {
        int nThreads = 100;
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(nThreads);

        for (int i = 0; i < nThreads; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    // Threads try to guess the same letter 'P'. 
                    // Since the method is synchronized, only the first should get it as 'not already used'.
                    // So attempts should not diminish, and used array shouldn't have 100 'P's.
                    state.processGuess("P");
                    
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown();
        doneLatch.await();
        executor.shutdown();

        // Attempts should remain intact since correct/recurring correct guess shouldn't drop it.
        assertEquals(Protocol.MAX_ATTEMPTS, state.getAttemptsLeft());
        // Used letters must contain exactly one "P"
        assertEquals("P", state.getUsedLettersString());
    }

    @Test
    void testConcurrentDecrementAttempts() throws InterruptedException {
        // Sometimes Server manually decrements attempts due to timeout
        int nThreads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(nThreads);

        for (int i = 0; i < nThreads; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    state.decrementAttempts();
                } catch (Exception e) { }
                finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertEquals(Protocol.MAX_ATTEMPTS - nThreads, state.getAttemptsLeft());
    }
}
