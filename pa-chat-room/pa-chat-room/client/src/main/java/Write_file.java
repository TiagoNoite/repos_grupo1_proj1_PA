import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Write_file  extends Thread {
    ArrayList<String> Buffer;

    public Write_file(ArrayList<String> buffer) {
        this.Buffer= buffer;
    }
    @Override
    public void run() {
        consumer_Thread();
    }

    public void consumer_Thread()  {
        String In_between = "";
        try {

            while(true) {
                if (Buffer.size() != 0) {
                    for (int i = 0; i < Buffer.size(); i++) {
                        In_between = In_between + Buffer.get(i) ;
                        //System.out.println("buffer no consumer:" + Buffer.get(i));
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
