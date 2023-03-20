import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
/**
 * The filter class is used as the name sugest filther the inapropriet word that are sent to the server and then send them back to the server
 */
public class filter extends Thread{
    private BlockingQueue<String>  filtro;
    BlockingQueue<String>  Buffer_unfilther;
    BlockingQueue<String>  Buffer_filther ;
    Semaphore Write_sem;
    /**
     * The Read_Files constructor, this takes one parameter to know where is the information to be collected
     *@param Buffer_unfilther    The buffer where is recieved the unfiltherd messages to be processed
     *@param Buffer_filtherd    the buffer where the filtered messages are put to be sent to the server
     *@param Write_sem    the semaphore that is uset to add the messages to the Buffer_filtherd
     */
    public filter(BlockingQueue<String>  Buffer_unfilther, BlockingQueue<String> Buffer_filtherd, Semaphore Write_sem){
        this.Buffer_unfilther=Buffer_unfilther;
        this.Buffer_filther=Buffer_filtherd;
        this.Write_sem= Write_sem;

    }

    /**
     * The replace_funcion is teh core of the filter, in here  if the unfiltherd buffer is not empty then the loop starts and retrieves the head
     * of the buffer and passes it to a variable, then splites it into an array and it takes evry space that ther is in evrey word of the array
     * then if one of teh wrds of the array is equal to the filter ther is replaced bye *** and the all the words on teh array are joined with
     * spaces beteen them and finaly the message is added to the filther buffer
     *
     */
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

    /**
     * the run method is where is called the replace_funcion to replace the words that should be removed, where is called the class to read files
     * to always read the file and add new words to the filter in case their added in the file and  then the thead sleeps 10 miliseconds
     *
     */
    @Override
    public void run() {

        while(true) {
            Read_Files readFiles = new Read_Files("C:\\Users\\pc\\IdeaProjects\\repos_grupo1_proj1_PA\\pa-chat-room\\filter.txt");
            readFiles.start();
            filtro = readFiles.read_file();

            replace_funcion();
            try {
                Thread.sleep(10); // sleep for 10 milliseconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
