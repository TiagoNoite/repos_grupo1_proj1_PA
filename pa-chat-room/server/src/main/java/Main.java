import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.BlockingQueue;
import java.util.Scanner;
import java.util.concurrent.*;
/**
 * The main class where it starts the code  and ist created the clients, arrays and the Write_file
 */
public class Main {
    public static void main ( String[] args ) {
        BlockingQueue<String> Buffer_unfilther = new LinkedBlockingQueue<>();
        BlockingQueue<String> Buffer_filtherd = new LinkedBlockingQueue<>();

        Semaphore Write_sem  = new Semaphore(1);


        filter filtro = new filter(Buffer_unfilther,Buffer_filtherd,Write_sem);
        ServerThread server = new ServerThread ( 8888,Buffer_unfilther, Buffer_filtherd, Write_sem);

        filtro.start ( );
        server.start ( );

    }
}
