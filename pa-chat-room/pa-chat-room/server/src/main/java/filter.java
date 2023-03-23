import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

public class filter extends Thread{
     private BlockingQueue<String>  filtro;
    BlockingQueue<String>  Buffer_unfilther;
    BlockingQueue<String>  Buffer_filther ;
    Semaphore Write_sem;

    public filter(BlockingQueue<String>  Buffer_unfilther, BlockingQueue<String> Buffer_filtherd, Semaphore Write_sem){
        this.Buffer_unfilther=Buffer_unfilther;
        this.Buffer_filther=Buffer_filtherd;
        this.Write_sem= Write_sem;

        Read_Files readFiles = new Read_Files("C:\\Users\\Genesis\\Downloads\\pa-chat-room_2_2\\pa-chat-room_2\\pa-chat-room\\filter.txt");
        readFiles.start();
        filtro = readFiles.read_file();
    }

    public void replace_funcion(){// falta adicionar a parte de ler as palavras que sao pa filtrar

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

    @Override
    public void run() {
        while(true) {
            replace_funcion();
            try {
                Thread.sleep(10); // sleep for 10 milliseconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
