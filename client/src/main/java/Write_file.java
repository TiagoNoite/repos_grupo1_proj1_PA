import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * <p>Class Write_file to write and keep the client history. Also ends the client's process</p>
 */
public class Write_file  extends Thread {
    ArrayList<String> Buffer;
    Semaphore endProcess;

    /**
     * <p>Class Write_file's Constructor</p>
     *
     * @param buffer Set of words that were to be written to the file
     * @param endProcess Allows to end the client's process
     */
    public Write_file(ArrayList<String> buffer, Semaphore endProcess) {
        this.Buffer= buffer;
        this.endProcess = endProcess;
    }

    @Override
    public void run() {
        consumer_Thread();
    }

    /**
     * <p>End the client's process</p>
     */
    public void consumer_Thread()  {
        String In_between = "";
        try {
            while(endProcess.availablePermits() != 0) {
                if (Buffer.size() != 0) {
                    for (int i = 0; i < Buffer.size(); i++) {
                        In_between = In_between + Buffer.get(i) ;
                        Buffer.remove(i);
                    }
                    Write_in_file(In_between);
                    In_between="";
                }else {
                   sleep(200);
                }
            }
        }
        catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }


    /**
     * <p>Function to keep server history</p>
     *
     * @param data Client's data which be saved in a document
     */
    public void Write_in_file(String data){
        boolean not_writen=false;
        while(!not_writen) {
            try {
                FileWriter fileWriter = new FileWriter("File_to_write.txt", true);
                BufferedWriter writer = new BufferedWriter(fileWriter);

                writer.write(data);
                writer.newLine();

                writer.close();
                fileWriter.close();

                not_writen=true;
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
    }
}
