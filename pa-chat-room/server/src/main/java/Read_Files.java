import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * The Read_Files class is used as the name sugest to read files more specificli from the filther
 */
public class Read_Files extends Thread {
    private String filename;
    /**
     * The Read_Files constructor, this takes one parameter to know where is the information to be collected
     *@param filename    the filename is the place where the information is
     *
     */
    public Read_Files(String filename ){
        this.filename=filename;
    }

    /**
     * The read_file method were is readed the file and the information is passed to a BlockingQueue and its returned
     *
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
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return words;
    }

    /**
     * the run method is where is called the read_file to read the files
     *
     */

}
