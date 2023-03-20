import java.util.ArrayList;
import java.util.concurrent.Semaphore;
/**
 * The main class where it starts the code  and ist created the clients, arrays and the Write_file
 */
public class Main {

    public static void main ( String[] args ) {

        ArrayList<String> Buffer = new ArrayList<>();
        Semaphore Write_sem  = new Semaphore(1);

        ClientThread client = new ClientThread ( 8888 , 1 , 1000,Buffer, Write_sem);
        ClientThread client2 = new ClientThread ( 8888 , 2 , 1000,Buffer, Write_sem);
        Write_file write= new Write_file(Buffer);

        client.start();
        client2.start();
        write.start();
    }
}
