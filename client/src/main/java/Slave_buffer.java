import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * The Slave_buffer class represents the slave that write in the buffer with semaphores to be sure that nobody else is using it
 */
public class Slave_buffer extends Thread {
    ArrayList<String> Buffer;
    String message;
    Semaphore Write_sem;

    /**
     * The Slave_buffer constructor,receives the buffer, message and the semaphore in witch this class should write
     * @param message       it's the messages it receives from the client to write in the buffer
     * @param Buffer        it's the buffer where the message will be written
     * @param Write_sem     this semaphore ensures that nobody else is using the buffer and that it can be written without causing any problem
     */
    public  Slave_buffer(ArrayList<String> Buffer, String message , Semaphore Write_sem){
        this.message=message;
        this.Buffer=Buffer;
        this.Write_sem= Write_sem;
    }

    /**
     * this method is the one that adds the message to the buffer and uses the semaphore to ensure that nobody is using it
     */

    public void access_buffer(){

        if(Write_sem.tryAcquire()) {
            Buffer.add(message);
            Write_sem.release();
        }

    }

    /**
     * the run method comes from the thread class and is used to create threads. In this class it calls the access_buffer to add to the buffer
     */
    @Override
    public void run() {
        access_buffer();
    }
}
