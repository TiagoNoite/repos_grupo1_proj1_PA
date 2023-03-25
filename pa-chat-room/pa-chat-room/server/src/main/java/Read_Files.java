import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Read_Files extends Thread {
    private String filename;

    public Read_Files(String filename ){
        this.filename=filename;
    }

    public BlockingQueue<String> read_file(){
        BlockingQueue<String> words = new LinkedBlockingQueue<>();
        try {
            File myObj = new File(filename);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                words.add(data);
            }
            myReader.close();

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred reading files.");
            e.printStackTrace();
        }
        return words;
    }
    @Override
    public void run() {
        while(true) {

            read_file();

        }
    }
}
