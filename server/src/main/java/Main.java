import java.util.concurrent.BlockingQueue;
import java.util.concurrent.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {
    public static void main ( String[] args ) {

        BlockingQueue<String> Buffer_unfilther = new LinkedBlockingQueue<>();
        BlockingQueue<String> Buffer_filtherd = new LinkedBlockingQueue<>();

        //Create Logger
        try {
            Logger logger = Logger.getLogger("MyLogger");
            FileHandler fh = new FileHandler("../server.log", true);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.addHandler(fh);
        }
        catch (Exception e){
            System.out.println(e);
        }


        Semaphore Write_sem  = new Semaphore(1);
        String filename = "./server.config";

        filter filtro = new filter(Buffer_unfilther,Buffer_filtherd,Write_sem);
        ServerThread server = new ServerThread ( 8888,Buffer_unfilther, Buffer_filtherd, Write_sem, filename);

        filtro.start ( );
        server.start ( );
    }
}
