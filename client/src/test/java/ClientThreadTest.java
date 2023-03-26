import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.*;

class ClientThreadTest {

    @Nested
    @DisplayName("construct the client and writer")
    class clients {
        private ArrayList<String> Buffer = new ArrayList<String>();
        private Semaphore Write_sem = new Semaphore(1);
        private Semaphore End_process = new Semaphore(1);
        private Write_file write = new Write_file(Buffer, End_process);
        private ClientThread client;

        @BeforeEach
        void setUp() {
            this.client= new ClientThread(8888, Buffer, Write_sem, End_process);
        }

        @Test
        void run() {
           // assertTimeout(Duration.ofSeconds(2), () -> client.run());
        }

        @Test
        void getClientState() {
            assertTimeout(Duration.ofSeconds(2), () -> client.getClientState());
        }

        @Test
        void activateState() {
            assertTimeout(Duration.ofSeconds(1), () -> client.activateState());
        }

        @Test
        void endState() {
            assertTimeout(Duration.ofSeconds(1), () -> client.endState());
        }

        @Test
        void getMaxNumberClients() {
            assertTimeout(Duration.ofSeconds(1), () -> client.getMaxNumberClients());
        }

        @Test
        void exit() {
           // assertTimeout(Duration.ofSeconds(2), () -> client.exit());
        }

        @Test
        void changeMaxClients() {
            assertTimeout(Duration.ofSeconds(2), () -> client.changeMaxClients());
        }

        @Test
        void writeMessage() {
            assertTimeout(Duration.ofSeconds(9), () -> client.writeMessage());
        }

        @Test
        void addToFilther() {
            assertTimeout(Duration.ofSeconds(2), () -> client.addToFilther());
        }

        @Test
        void menu() {
            assertTimeout(Duration.ofSeconds(2), () -> client.menu());
        }

        @Test
        void showMenu() {
            assertTimeout(Duration.ofSeconds(2), () -> client.showMenu());
        }

    }


}
