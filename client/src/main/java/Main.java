import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Main {
    public static void main ( String[] args ){

        ArrayList<String> Buffer = new ArrayList<>();
        Semaphore Write_sem  = new Semaphore(1);
        Semaphore endProcess = new Semaphore(1);

        Write_file write= new Write_file(Buffer, endProcess);
        write.start();

        ClientThread client = new ClientThread ( 8888,Buffer, Write_sem, endProcess);
        client.start();
    }
}
