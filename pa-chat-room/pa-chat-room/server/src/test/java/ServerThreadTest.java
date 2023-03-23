import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import static org.junit.jupiter.api.Assertions.*;

class ServerThreadTest {
    private ServerThread server;
    private ArrayList<String> Buffer;
    private LinkedBlockingQueue<String> Buffer_unfilther;
    private Semaphore semaphore;
    private Semaphore Write_sem;
    private String filename = "\\server\\server.config";
    private BlockingQueue<String> Buffer_filtherd;
    private ClientThread c1, c2, c3, c4;

    @BeforeEach
    @DisplayName("")
    void setUp() {
        this.Buffer = new ArrayList<>();
        this.Buffer_unfilther = new LinkedBlockingQueue<>();
        this.semaphore = new Semaphore(1);
        this.Write_sem = new Semaphore(1);
        this.Buffer_filtherd  = new LinkedBlockingQueue<>();

        this.c1 = new ClientThread(8888,  Buffer, semaphore);
        this.c2 = new ClientThread(8888,  Buffer, semaphore);
        this.c3 = new ClientThread(8888,  Buffer, semaphore);
        this.c4 = new ClientThread(8888,  Buffer, semaphore);

        this.server = new ServerThread ( 8888,Buffer_unfilther, Buffer_filtherd, Write_sem, filename);;
    }

    @Test
    void getMaxClient() {
        assertEquals(3, server.getMaxClient(filename));
    }

    @Test
    void addClient() throws InterruptedException {
        server.connectClient(1);
        server.connectClient(2);
        assertTrue(server.getSemaphore().tryAcquire());
        server.getSemaphore().release();
        server.connectClient(3);
        assertEquals(3, server.getSizeOfClients());
        assertFalse(server.getSemaphore().tryAcquire());
    }

    @Test
    void removeClient() throws InterruptedException {
        server.connectClient(1);
        server.connectClient(2);
        server.connectClient(3);
        assertEquals(3, server.getSizeOfClients());
        server.disconnectClient(1);
        server.disconnectClient(2);
        assertEquals(1, server.getSizeOfClients());
    }
}