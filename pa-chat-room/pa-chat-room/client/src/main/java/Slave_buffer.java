import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Slave_buffer extends Thread {
    ArrayList<String> Buffer;
    String message;
    Semaphore Write_sem;
    public  Slave_buffer(ArrayList<String> Buffer, String message , Semaphore Write_sem){
        this.message=message;
        this.Buffer=Buffer;
        this.Write_sem= Write_sem;
    }

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
