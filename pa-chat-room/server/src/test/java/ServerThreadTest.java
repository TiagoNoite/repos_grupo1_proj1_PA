import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.*;

class ServerThreadTest {

    @Nested
    @DisplayName("construct the filther")
    class serve {

        private BlockingQueue<String> BufferUnfilterd = new LinkedBlockingQueue<String>();
        private BlockingQueue<String> BufferFilterd = new LinkedBlockingQueue<String>();
        private Semaphore Write_sem = new Semaphore(1);
        private filter filther;
        private ServerThread server;

        @BeforeEach
        void setUp() {
            this.BufferUnfilterd=BufferUnfilterd;
            this.server = new ServerThread ( 8888,BufferUnfilterd, BufferFilterd, Write_sem);
            this.filther= new filter(BufferUnfilterd, BufferFilterd, Write_sem);
            filther.start();
        }

        @Test
        void run() {
            assertTimeout(Duration.ofSeconds(1) , () ->  server.run());
        }
    }
}