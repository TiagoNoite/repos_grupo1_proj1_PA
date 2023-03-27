import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.assertTimeout;

class filterTest {
    @Nested
    @DisplayName("construct the filther")
    class filther {

        private BlockingQueue<String> BufferUnfilterd= new LinkedBlockingQueue<String>();
        private BlockingQueue<String> BufferFilterd= new LinkedBlockingQueue<String>();
        private Semaphore Write_sem  = new Semaphore(1);
        private filter filther;


        @BeforeEach
        void setUp() {
            this.BufferUnfilterd=BufferUnfilterd;
            BufferUnfilterd.add("hello");
            BufferUnfilterd.add("ola");
            BufferUnfilterd.add("adeus");
            BufferUnfilterd.add("aaaaa");
            this.filther= new filter(BufferUnfilterd, BufferFilterd, Write_sem);
        }

               @Test
        void replace_funcion(){
            assertTimeout(Duration.ofSeconds(1) , () ->  filther.replace_funcion());
        }


        @Test
        void run() {
           // assertTimeout(Duration.ofSeconds(1) , () ->  filther.run());
        }
    }
    }
}
