import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <p>Read the data from the files</p>
 */
public class Read_Files extends Thread {
    private String filename;

    /**
     * <p>class Read_Files's Constructor</p>
     * @param filename path where the server are going to search the data
     */
    public Read_Files(String filename ){
        this.filename=filename;
    }

    /**
     *<p>Read the data from the files</p>
     * @return BlockingQueue
     */
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
