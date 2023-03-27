import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import org.junit.jupiter.api.*;
class Slave_bufferTest {
    @Nested
    @DisplayName("construct the slave")
    class slave {
        private ArrayList<String> Buffer = new ArrayList<String>();
        private String message = "ola";
        private Semaphore Write_sem = new Semaphore(1);
        private Slave_buffer slave= new Slave_buffer(Buffer,message,Write_sem);
        @BeforeEach
        void setUp() {

            Buffer.add(message);
        }

        @Test
        void access_buffer() {
            assertTimeout(Duration.ofSeconds(2), () -> slave.access_buffer());
        }

        @Test
        void run() {
         //   assertTimeout(Duration.ofSeconds(2), () -> slave.run());
        }
    }
}
