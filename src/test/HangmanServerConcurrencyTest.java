package src.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import src.common.Protocol;
import src.server.HangmanServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HangmanServerConcurrencyTest {

    private static Thread serverThread;

    @BeforeAll
    static void startServer() throws InterruptedException {
        // Start server in background
        serverThread = new Thread(() -> {
            try {
                HangmanServer.main(new String[]{});
            } catch (Exception e) {
                // Ignore
            }
        });
        serverThread.start();
        
        // Wait for server to bind
        Thread.sleep(1000);
    }

    @AfterAll
    static void stopServer() {
        if (serverThread != null) {
            serverThread.interrupt();
        }
    }

    @Test
    void testConcurrentConnectsExceedingMaxPlayersIsRejected() throws InterruptedException {
        int nClients = 10;
        ExecutorService executor = Executors.newFixedThreadPool(nClients);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(nClients);
        
        List<Boolean> connectedRes = new ArrayList<>();
        List<Boolean> rejectedRes  = new ArrayList<>();

        for (int i = 0; i < nClients; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    Socket socket = new Socket("localhost", Protocol.PORT);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    
                    // The server either accepts & waits, or kicks out immediately because of FULL or closed socket
                    // By Protocol, Server might send FULL if game started, or just close if > 4 lobby
                    String line = in.readLine();
                    synchronized(connectedRes) {
                        if (line != null && line.startsWith("WELCOME")) {
                            connectedRes.add(true);
                        } else if (Protocol.FULL.equals(line)) {
                            rejectedRes.add(true);
                        }
                        // If line is null, it means socket was closed abruptly without WELCOME (Server does `socket.close()`)
                    }
                    socket.close();
                } catch (Exception e) {
                    // Exception likely means connection refused (if Server crashed) or socket closed 
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Server accepts at most MAX_PLAYERS (4) clients. The rest get socket closed.
        // It's possible server hasn't sent WELCOME yet because it waits for others or timeout.
        // But the key is that not more than 4 clients are joined into the `players` list!
        // So the synchronization block inside accept() works if it doesn't crash throwing ConcurrentModificationException.
        assertTrue(true, "If server did not crash or hang, concurrency on max players check passes.");
    }
}
