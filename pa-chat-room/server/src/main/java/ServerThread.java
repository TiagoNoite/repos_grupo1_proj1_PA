import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.concurrent.BlockingQueue;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
/**
 * The ServerThread class represents server that receives messages from the client and sends a response to them
 */
public class ServerThread extends Thread {
    private final int port;
    private DataInputStream in;
    private PrintWriter out;
    private ServerSocket server;
    private Socket socket;
    BlockingQueue<String>  Buffer_unfilther;
    BlockingQueue<String>  Buffer_filther ;
    Semaphore Write_sem;
    /**
     * The ServerThread constructor, this one specefies the port in where it will begin and has buffers to put and receive the messages from the
     * clients and the ones that are filtherd
     * @param port              the port where the messages are receive and sent
     * @param Buffer_unfilther  This buffer is where the messages that are not filthed are added to be processed
     * @param Buffer_filther    This buffer is where the messages that are filthed are added to be sent to the server and showed
     * @param Write_sem         This semaphore was used before to create a slave to add the messages to the buffer
     */
    public ServerThread ( int port,  BlockingQueue<String>  Buffer_unfilther, BlockingQueue<String>  Buffer_filther,Semaphore Write_sem ) {
        this.port = port;
        this.Buffer_unfilther = Buffer_unfilther;
        this.Buffer_filther = Buffer_filther;
        this.Write_sem=Write_sem;
        try {
            server = new ServerSocket ( this.port );
        } catch ( IOException e ) {
            e.printStackTrace ( );
        }
    }
    /**
     * the run method is the core of the server, it's where the server listens and responds to the clients, here is added to the buffer
     * not filtered the messages received from teh clients
     */
    public void run ( ) {

        while ( true ) {
            try {

                System.out.println ( "Accepting Data" );
                socket = server.accept ( );
                in = new DataInputStream ( socket.getInputStream ( ) );
                out = new PrintWriter ( socket.getOutputStream ( ) , true );

                //WaitBuffer responde_mensages = new WaitBuffer(Buffer_filther, out, Write_sem);
                //responde_mensages.start();

                String message = in.readUTF( );
                Buffer_unfilther.add(message);

                String replaced= Buffer_filther.take();

                out.println(replaced);
                System.out.println(replaced);

                //responde_mensages.join();

            } catch ( IOException e ) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
