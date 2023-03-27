import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * <p>Class Slave_buffer where the client's messages are saved and control</p>
 */

public class Slave_buffer extends Thread {
    ArrayList<String> Buffer;
    String message;
    Semaphore Write_sem;

    /**
     * <p>Class Slave_buffer's Constructor</p>
     *
     * @param Buffer Memory where the messages are saved
     * @param message Message that the client sent to the server
     * @param Write_sem Control the entry and exit of the messages
     *
     */
    public  Slave_buffer(ArrayList<String> Buffer, String message , Semaphore Write_sem){
        this.message=message;
        this.Buffer=Buffer;
        this.Write_sem= Write_sem;
    }

    /**
     * <p>Allows to the client to send a message to the server</p>
     */
    public void access_buffer(){
        if(Write_sem.tryAcquire()) {
            Buffer.add(message);
            Write_sem.release();
        }
    }

    @Override
    public void run() {
        access_buffer();
    }
}
