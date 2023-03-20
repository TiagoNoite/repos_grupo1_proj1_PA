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

public class ServerThread extends Thread {
    private final int port;
    private DataInputStream in;
    private PrintWriter out;
    private ServerSocket server;
    private Socket socket;
    BlockingQueue<String>  Buffer_unfilther;
    BlockingQueue<String>  Buffer_filther ;
    Semaphore Write_sem;

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

    public void run ( ) {

        boolean enterd= false;

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


//http://www.cs.sjsu.edu/~pearce/modules/lectures/oop/templates/threads/index.htm
}
