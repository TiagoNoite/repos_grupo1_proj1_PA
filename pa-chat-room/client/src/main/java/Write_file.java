import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
/**
 * The Write_file class is the class that write in the file the messages that are in the buffer
 */
public class Write_file  extends Thread {
    ArrayList<String> Buffer;
    /**
     * The Write_file constructor,it receives the buffer
     * @param buffer     its where it takes the messages to write them in the file
     */
    public Write_file(ArrayList<String> buffer) {
        this.Buffer= buffer;
    }

    /**
     * the run method comes from the thread class it calls the consumer_Thread in witch does all it need to write in the file the messages
     */
    @Override
    public void run() {

        consumer_Thread();
    }
    /**
     * consumer_Thread is teh method that makes a infinite loop where it receives the buffer, takes all the information inside of it and removes
     * it, to pass it to a string created every cicle and this string is the passed to the method that writes in the file. it has a try catch  in
     * case the is any problem with teh sleep function
     *
     */
    public void consumer_Thread()  {
        String In_between = "";
        try {

            while(true) {

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
     * The Write_file it takes a string as a parameter and writes this string in the file by creating a FileWriter and a BufferedWriter to write on it
     * in case the is any problem accessing the file it has a try catch to catch exceptions
     * @param data     It's where it takes the messages to write them in the file
     *
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
