import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * <p>Class Filter to filter the messages and data for each client that send to the server</p>
 *
 */
public class filter extends Thread{
     private BlockingQueue<String>  filtro;
    BlockingQueue<String>  Buffer_unfilther;
    BlockingQueue<String>  Buffer_filther ;
    Semaphore Write_sem;

    /**
     * <p>Class Filter's Constructor</p>
     *
     * @param Buffer_filtherd List of messages that have already passed from the filter
     * @param Buffer_unfilther List of messages that haven't passed from the filter
     * @param Write_sem control
     *
     */
    public filter(BlockingQueue<String>  Buffer_unfilther, BlockingQueue<String> Buffer_filtherd, Semaphore Write_sem){
        this.Buffer_unfilther=Buffer_unfilther;
        this.Buffer_filther=Buffer_filtherd;
        this.Write_sem= Write_sem;

        Read_Files readFiles = new Read_Files("C:\\Users\\pc\\Desktop\\universidade\\3_ano\\PA\\praticas\\repos_grupo1_proj1_PA-Final\\filter.txt");
        readFiles.start();
        filtro = readFiles.read_file();
    }


    public void replace_funcion(){

        while(!Buffer_unfilther.isEmpty()) {
            String word = Buffer_unfilther.poll();

            String[] Split = word.split("\\s+");

            for (int i = 0; i < Split.length; i++) {
                Split[i] = Split[i].replaceAll("[^\\w]", "");
            }

            for (int i = 0; i < Split.length; i++) {
                if (filtro.contains(Split[i])) {
                    Split[i] = "******";
                }
            }

            String replaced = "";
            for (int i = 0; i < Split.length; i++) {
                replaced += Split[i] + " ";
            }

            if(Write_sem.tryAcquire()) {
                Buffer_filther.add(replaced);
                Write_sem.release();
            }
        }
    }

    public void pre_run() {
        replace_funcion();
        try {
            Thread.sleep(10); // sleep for 10 milliseconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(true) {
            pre_run();
        }
    }
}
