import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.Scanner;
/**
 * The main class where it starts the code  and ist created the clients, arrays and the Write_file
 */
public class Main extends Thread {


    public static void main ( String[] args ) {
        Scanner myObj = new Scanner(System.in);

        ArrayList<String> Buffer = new ArrayList<String>();
        Semaphore Write_sem  = new Semaphore(1);
       // Semaphore send_message  = new Semaphore(1);
       // int chooseClient=0;
        //boolean message_deliverd=false;


        ClientThread client = new ClientThread ( 8888 , 1 , 1000,Buffer, Write_sem);
        ClientThread client2 = new ClientThread ( 8888 , 2 , 1000,Buffer, Write_sem);
        Write_file write= new Write_file(Buffer,"File_to_write.txt");

        write.start();
        client.start();
        client2.start();


    }
}
