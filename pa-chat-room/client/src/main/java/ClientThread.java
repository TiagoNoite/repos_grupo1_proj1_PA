import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;
import java.io.*;   // Import the FileWriter class
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;

/**
 * The ClientThread class represents Client that speaks to the server and creates a slave to put its messages in a buffer
 */
public class ClientThread extends Thread {
    private final int port;
    private final int id;
    private final int freq;
    private DataOutputStream out;
    private BufferedReader in;
    private Socket socket;
    private Scanner myObj = new Scanner(System.in);
    ArrayList<String> Buffer;
    Semaphore Write_sem;

    /**
     * The ClientThread constructor, it specifies the id, port and frequencies to speak to the server and gives the buffer and the semaphore
     * to send teh messages to the slave and then to the file
     * @param id        an unique client ID
     * @param port      the port to listen or send the message to the server
     * @param freq      tells how much time the client must sleep or be inactive
     * @param Buffer    this buffer is shared to another class and the client creates a slave to write in this buffer
     * @param Write_sem this semaphore is passed to the slave created by th client and is used to not allow to many people using the buffer
     */
    public ClientThread ( int port , int id , int freq, ArrayList<String> Buffer, Semaphore Write_sem ) {
        this.port = port;
        this.id = id;
        this.freq = freq;
        this.Buffer = Buffer;
        this.Write_sem =Write_sem;
    }

    /**
     * the run method comes from the thread class and is used to create threads. Here is call the method producer_Thread that makes the client
     * function, and it does not take any parameters
     */
    public void run () {
        producer_Thread();
        /*
         https://www.geeksforgeeks.org/producer-consumer-solution-using-threads-java/
        */

    }

    /**
     * the producer_Thread does not take any parameters, and it is the core of this class, it creates the path between this client and the server
     * it sends and receives messages from the server, creates the slave to put the information in the buffer and send it to be written in the file
     * and waits for a certain amount of time to be active again
     *
     */
    public void producer_Thread(){
        int i = 0;
        while ( true ) {
            try {
                // if(sem.tryAcquire(1, TimeUnit.SECONDS)) {
                socket = new Socket ( "localhost" , port );
                out = new DataOutputStream ( socket.getOutputStream ( ) );
                in = new BufferedReader ( new InputStreamReader ( socket.getInputStream ( ) ) );

                System.out.println("write your message: ");
                String message = myObj.nextLine();  // Read user input

                out.writeUTF("Message is: " + message + " from cliente"+ id);

                Slave_buffer add_buff= new Slave_buffer(Buffer, message, Write_sem);
                add_buff.start();

                String response;
                response = in.readLine ( );
                System.out.println ( "From Server: " + response );
                out.flush ();
                socket.close ( );
                sleep ( freq );
                i++;

                add_buff.join();

            } catch ( IOException | InterruptedException e ) {
                e.printStackTrace ( );
            }
        }
    }

}
