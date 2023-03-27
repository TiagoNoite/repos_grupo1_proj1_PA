import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
class Read_FilesTest {
    @Nested
    @DisplayName("construct the file reader")
    class reader {
        private Read_Files reader =new Read_Files("./../filter.txt");
        
        @Test
        void read_file() {
            reader.read_file();
        }
    }
}
