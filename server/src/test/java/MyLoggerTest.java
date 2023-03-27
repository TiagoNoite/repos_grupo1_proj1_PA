
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.junit.jupiter.api.Assertions.*;

class MyLoggerTest {
    @Nested
    @DisplayName("construct the logger")
    class test {
        private MyLogger testLogger;

        @BeforeEach
        public void test() {
            try {
                Logger testLogger = Logger.getLogger("MyLogger");
                FileHandler fh = new FileHandler("../server.log", true);
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);
                testLogger.addHandler(fh);
                this.testLogger = new MyLogger();
            } catch (Exception e) {
                System.out.println(e);
            }

        }

        @Test
        void logNewMessage() {
            assertTimeout(Duration.ofSeconds(3), () -> testLogger.logNewMessage(LogType.CONNECTION, 1, "ola"));
            assertTimeout(Duration.ofSeconds(3), () -> testLogger.logNewMessage(LogType.DISCONNECTION, 1, "ola"));
            assertTimeout(Duration.ofSeconds(3), () -> testLogger.logNewMessage(LogType.WAITING, 1, "ola"));
            assertTimeout(Duration.ofSeconds(3), () -> testLogger.logNewMessage(LogType.MESSAGE, 1, "ola"));
        }
    }

}
