import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
class Read_FilesTest {
    @Nested
    @DisplayName("construct the file reader")
    class reader {
        private Read_Files readFiles = new Read_Files("C:\\Users\\pc\\Desktop\\universidade\\3_ano\\PA\\praticas\\repos_grupo1_proj1_PA-Final\\filter.txt");

        @Test
        void read_file() {
            reader.read_file();
        }
    }
}
