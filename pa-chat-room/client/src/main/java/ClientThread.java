import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.io.*;   // Import the FileWriter class
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;


public class ClientThread extends Thread {
    private final int port;
    private DataOutputStream out;
    private BufferedReader in;
    private Socket socket;
    private Scanner myObj = new Scanner(System.in);

    //private final Semaphore write_sem;
     ArrayList<String> Buffer;
    Semaphore Write_sem;


    public ClientThread ( int port, ArrayList<String> Buffer, Semaphore Write_sem ) {
        this.port = port;
        this.Buffer = Buffer;
        this.Write_sem =Write_sem;
        try {
            this.socket = socket = new Socket("localhost", port);
            this.out = new DataOutputStream ( socket.getOutputStream ( ) );
            this.in = new BufferedReader ( new InputStreamReader ( socket.getInputStream ( ) ) );
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    public void run () {
        /*
         https://www.geeksforgeeks.org/producer-consumer-solution-using-threads-java/
        */

        try {
            while(true) {
                System.out.println("Write your message: ");
                // Read user input
                String message = myObj.nextLine();


                // Send server my request
                out.writeUTF(message);
                out.flush();

                Slave_buffer add_buff = new Slave_buffer(Buffer, message, Write_sem);
                add_buff.start();


                //Read response from server
                String response = in.readLine();

                System.out.println("From Server: " + response);

                add_buff.join();
            }
        } catch ( IOException | InterruptedException e ) {
            e.printStackTrace ( );
        }

        try {
            socket.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }


}
