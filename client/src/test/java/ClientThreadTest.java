import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.*;

class ClientThreadTest {



    @Nested
    @DisplayName("construct the client and writer")
    class clients{

        private Write_file write;
        private ArrayList<String> Buffer = new ArrayList<String>();
        private Semaphore Write_sem  = new Semaphore(1);

        private ClientThread client= new ClientThread(8888,1,1,Buffer,Write_sem);

        @BeforeEach
        void setUp() {

        }
        @Test
        void run() {
            assertTimeout(Duration.ofSeconds(2) , () ->  client.run());
        }
        @Test
        void producer_Thread() {
            //assertThrows(IOException.class, ()->client.producer_Thread());
           //assertThrows(InterruptedException.class, ()->client.producer_Thread());
            assertTimeout(Duration.ofSeconds(2) , () ->  client.producer_Thread());
        }

    }

}