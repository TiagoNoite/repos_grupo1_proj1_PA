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


public class ClientThread extends Thread {
    private final int port;
    private final int id;
    private final int freq;
    private DataOutputStream out;
    private BufferedReader in;
    private Socket socket;
    private Scanner myObj = new Scanner(System.in);

    //private final Semaphore write_sem;
     ArrayList<String> Buffer;
    Semaphore Write_sem;


    public ClientThread ( int port , int id , int freq, ArrayList<String> Buffer, Semaphore Write_sem ) {
        this.port = port;
        this.id = id;
        this.freq = freq;
        this.Buffer = Buffer;
        this.Write_sem =Write_sem;
    }

    public void run () {
        producer_Thread();
        /*
         https://www.geeksforgeeks.org/producer-consumer-solution-using-threads-java/
        */

    }

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
                   // out.writeUTF("Message is: ola adeus fuck frick aaaaaa from cliente"+ id);

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
