import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

public class WaitBuffer extends Thread {
    BlockingQueue<String> buffer;
    PrintWriter out;
    Semaphore writeSem;
    public WaitBuffer(BlockingQueue<String> buffer, PrintWriter out, Semaphore writeSem) {
        this.buffer = buffer;
        this.out = out;
        this.writeSem = writeSem;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String message = buffer.take();
                System.out.println("Processing message: " + message);
                writeSem.acquire();

                writeSem.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
