import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.*;
 
class ClientHandlerTest {
    private ServerThread server;
    private Socket socket;
    private ClientHandler clientHandler;
    private ArrayList<String> Buffer;
    private LinkedBlockingQueue<String> Buffer_unfilther;
    private Semaphore semaphore;
    private Semaphore Write_sem;
    private Semaphore si;
    private String filename = "../server.config";
    private BlockingQueue<String> Buffer_filtherd;
    private DataOutputStream out;
    private BufferedReader in;
    //private DataInputStream DIS = new DataInputStream(this.clientSocket.getInputStream());;
    //private PrintWriter PW;

    private static Integer portAdd = 8080;
    //private DataInputStream DIS = new DataInputStream(this.clientSocket.getInputStream());;
    //private PrintWriter PW;

    @BeforeEach
    void setUp() {
        try {
            this.socket = new Socket("localhost", 8888);
            this.out = new DataOutputStream( socket.getOutputStream ( ) );
            this.in = new BufferedReader( new InputStreamReader( socket.getInputStream()));
        }
        catch (Exception e){
            System.out.println(e);
        }
        this.Buffer = new ArrayList<>();
        this.Buffer_unfilther = new LinkedBlockingQueue<>();
        this.semaphore = new Semaphore(1);
        this.Write_sem = new Semaphore(1);
        this.Buffer_filtherd  = new LinkedBlockingQueue<>();
        this.si = new Semaphore(1);
        portAdd += 1;
        this.server = new ServerThread ( portAdd,Buffer_unfilther, Buffer_filtherd, Write_sem, filename);
        this.clientHandler= new ClientHandler(Buffer_unfilther, Buffer_filtherd,socket,1,server);
    }
    @Test
    void serverResponse() {
        //assertEquals(3, clientHandler.serverResponse(DIS,Pw));
    }

    @Test
    void run() {
       // assertTimeout(Duration.ofSeconds(2), () -> clientHandler.run());
    }

}
