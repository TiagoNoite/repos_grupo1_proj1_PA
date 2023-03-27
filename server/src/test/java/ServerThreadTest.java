import org.junit.jupiter.api.BeforeEach;
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
    private Semaphore si;
    private String filename = "../server.config";
    private BlockingQueue<String> Buffer_filtherd;

    @BeforeEach
    void setUp() {
        this.Buffer = new ArrayList<>();
        this.Buffer_unfilther = new LinkedBlockingQueue<>();
        this.semaphore = new Semaphore(1);
        this.Write_sem = new Semaphore(1);
        this.Buffer_filtherd  = new LinkedBlockingQueue<>();
        this.si = new Semaphore(1);
        this.server = new ServerThread ( 8888,Buffer_unfilther, Buffer_filtherd, Write_sem, filename);
    }

    @Test
    void getMaxClientFromServer() {
        assertEquals(3, server.getMaxClientFromServer(filename));
    }

    @Test
    void addClient() throws InterruptedException {
        //Add 2 clients to the server
        server.addClient();
        server.connectClient();
        server.addClient();
        server.connectClient();

        //Ask if can add another client
        assertTrue(server.getSemaphore().tryAcquire());
        server.getSemaphore().release();

        //Add other client
        server.addClient();
        server.connectClient();

        //Check if there are 3 client on the server
        assertEquals(3, server.getCurrentClients());
        assertFalse(server.getSemaphore().tryAcquire());
    }

    @Test
    void removeClient() throws InterruptedException {
        //Add 3 clients to the server
        server.addClient();
        server.connectClient();
        server.addClient();
        server.connectClient();
        server.addClient();
        server.connectClient();

        //Verify id there 3 clients on the server
        assertEquals(3, server.getCurrentClients());

        //Delete one client
        server.disconnectClient();

        //Verify if the client go away, now there are 2 client on the server
        assertEquals(2, server.getCurrentClients());
    }

    @Test
    void getCurrentClients() throws InterruptedException {
        //Add 2 clients on the server
        server.addClient();
        server.connectClient();
        server.addClient();
        server.connectClient();

        //Check if the clients connect to the server
        assertEquals(2, server.getCurrentClients());
    }

    @Test
    void connectClient() throws InterruptedException {
        //Add 3 clients on the server
        server.addClient();
        server.connectClient();
        server.addClient();
        server.connectClient();
        server.addClient();
        server.connectClient();

        //Ask if the server can have another client, return false because the server is full
        assertFalse(server.connectClient());
        assertEquals(3, server.getCurrentClients());

        //Disconnect 1 client
        server.disconnectClient();

        //Ask to the server if can have another client, true because the server has space
        assertTrue(server.connectClient());
        assertEquals(2, server.getCurrentClients());
    }

    @Test
    void disconnectClient() throws InterruptedException {
        //Add 2 clients on the server
        server.addClient();
        server.connectClient();
        server.addClient();
        server.connectClient();

        //Check if the clients connect to the server
        assertEquals(2, server.getCurrentClients());

        //Disconnect 1 client
        server.disconnectClient();

        //Check if the client was disconnected
        assertEquals(1, server.getCurrentClients());

        //Disconnect 1 client
        server.disconnectClient();

        //Check if the client was disconnected
        assertEquals(0, server.getCurrentClients());
    }

    @Test
    void editMaxNumber() {
        //Ask the quantity of client that the server can allow
        assertEquals(3, server.getMaxClients());

        //Change that quantity, in this case increments the quantity
        server.setMaxClients(5);

        //Ask if the change is apply
        assertEquals(5, server.getMaxClients());

        //Change that quantity, in this case decreases the quantitY
        server.setMaxClients(2);

        //Ask if the change is apply
        assertEquals(2, server.getMaxClients());
    }
}