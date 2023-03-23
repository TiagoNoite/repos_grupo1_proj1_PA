import java.time.Duration;
import java.util.ArrayList;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
class Write_fileTest {
    @Nested
    @DisplayName("construct the writer")
    class writer {

        private ArrayList<String> Buffer = new ArrayList<String>();
        private ArrayList<String> Buffer2 = new ArrayList<String>();
        private Write_file write = new Write_file(Buffer, "File_to_write.txt") ;
        private Write_file write2 = new Write_file( Buffer2, "a") ;


        @BeforeEach
        void setUp() {
            this.write= write;
            this.Buffer= Buffer;
            this.write2= write2;
            this.Buffer2= Buffer2;
            Buffer.add("ola");
            Buffer.add("adeus");
            Buffer.add("boa tarde");
            Buffer.add("tchau");
        }
        @Test
        void run() {
            assertTimeout(Duration.ofSeconds(2) , () ->  write2.run());

        }

        @Test
        void consumer_Thread() {
            assertTimeout(Duration.ofSeconds(2) , () ->  write.consumer_Thread());
            assertTimeout(Duration.ofSeconds(2) , () ->  write2.consumer_Thread());
        }

        @Test
        void write_in_file() {
            assertTimeout(Duration.ofSeconds(1) , () ->  write.Write_in_file("ola"));
            assertTimeout(Duration.ofSeconds(1) , () ->  write2.Write_in_file("1"));
        }
    }
}