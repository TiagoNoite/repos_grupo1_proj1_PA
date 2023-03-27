
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

import java.time.Duration;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.junit.jupiter.api.Assertions.*;

class MyLoggerTest {

    private MyLogger testLogger;

    @BeforeAll
    public void test(){
        try {
            Logger testLogger = Logger.getLogger("MyLogger");
            FileHandler fh = new FileHandler("../server.log", true);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            testLogger.addHandler(fh);
            this.testLogger = new MyLogger();
        }
        catch (Exception e){
            System.out.println(e);
        }

    }

    @Test
    @Tag("Server.log test")
    @DisplayName("Log Connection")
    public void serverLogTestCon(){
        this.testLogger.logNewMessage(LogType.CONNECTION, 0, null);
        assertTimeout(Duration.ofSeconds(3), () -> {
            this.testLogger.logNewMessage(LogType.CONNECTION, 0, null);
        });
    }
    @Test
    @Tag("Server.log test")
    @DisplayName("Log Disconnection")
    public void serverLogTestDis(){
        assertTimeout(Duration.ofSeconds(3), () -> {
            this.testLogger.logNewMessage(LogType.DISCONNECTION, 0, null);
        });
    }

    @Test
    @Tag("Server.log test")
    @DisplayName("Log Waiting")
    public void serverLogTestWai(){
        assertTimeout(Duration.ofSeconds(3), () -> {
            this.testLogger.logNewMessage(LogType.WAITING, 0, null);
        });
    }

    @Test
    @Tag("Server.log test")
    @DisplayName("Log Message")
    public void serverLogTestMes(){
        assertTimeout(Duration.ofSeconds(3), () -> {
            this.testLogger.logNewMessage(LogType.MESSAGE, 0, "this is a message");
        });
    }
}