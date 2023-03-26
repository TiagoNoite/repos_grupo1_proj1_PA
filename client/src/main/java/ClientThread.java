import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;
import java.util.logging.Logger;


public class ClientThread extends Thread {
    private final int port;
    private DataOutputStream out;
    private BufferedReader in;
    private Socket socket;
    private Scanner myObj = new Scanner(System.in);
    private ArrayList<String> Buffer;
    private Semaphore Write_sem;
    private ClientState state;
    private Semaphore endProcess;


    public ClientThread ( int port, ArrayList<String> Buffer, Semaphore Write_sem, Semaphore endProcess ) {
        this.port = port;
        this.Buffer = Buffer;
        this.Write_sem =Write_sem;
        this.state = ClientState.WAIT;
        this.endProcess = endProcess;

        try {
            this.socket = new Socket("localhost", port);
            this.out = new DataOutputStream ( socket.getOutputStream ( ) );
            this.in = new BufferedReader ( new InputStreamReader ( socket.getInputStream ( ) ) );
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    /**
     * Gets actual client's state
     *
     * @return the actual state of the client
     */
    public ClientState getClientState() {
        return state;
    }

    /**
     * Change the actual state of the client to OK, Which activated the client
     */
    public void activateState() {
        this.state = ClientState.OK;
    }

    /**
     * Change the actual state of the client to end, Which indicated that the client has been disconnected
     */
    public void endState(){
        this.state = ClientState.END;
    }

    /**
     * Gets the actual maximum number of client that the server allow
     *
     * @throws IOException Signals that an I/O exception of some sort has occurred
     */
    public void getMaxNumberClients() throws IOException {
        out.writeUTF("get");
        String str = in.readLine();

        System.out.println(str);
    }


    /**
     * Finish the client's process
     *
     * @throws IOException Signals that an I/O exception of some sort has occurred
     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted, either before or during the activity.
     */
    public void exit() throws IOException, InterruptedException {
        out.writeUTF("exit");
        String shutdown = in.readLine();
        endState();

        if (shutdown.equals("SHUTDOWN")){
            this.endProcess.acquire();
        }
    }

    /**
     * Change the maximum number of clients that the server allow
     *
     * @throws IOException Signals that an I/O exception of some sort has occurred
     */
    public void changeMaxClients () throws IOException{
        out.writeUTF("edit");

        if (in.readLine().equals("NEW NUMBER")){
            System.out.print("Write the new number of max clients: ");
            // Read user input
            String maxClients = myObj.nextLine();

            out.writeUTF(maxClients);
        }
    }

    /**
     * Allow the client to write messages on the server
     *
     * @throws IOException Signals that an I/O exception of some sort has occurred
     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted, either before or during the activity.
     */
    public void writeMessage() throws IOException, InterruptedException {
        out.writeUTF("write");
        System.out.println("Write your message: ");
        // Read user input
        String message = myObj.nextLine();
        // Send server my request
        out.writeUTF(message);
        out.flush();
        Slave_buffer add_buff = new Slave_buffer(Buffer, message, Write_sem);
        add_buff.start();

        //Read response from server
        String response = in.readLine();

        System.out.println("From Server: " + response);

        add_buff.join();
    }

    /**
     * Allow the client to write new word on the filter file
     *
     * @throws IOException Signals that an I/O exception of some sort has occurred
     */
    public void addToFilther () throws IOException{

        System.out.print("Write the word you want to add to the filter ");

        String word = myObj.nextLine();

        try {
            FileWriter fileWriter = new FileWriter("./filter.txt", true);
            BufferedWriter writer = new BufferedWriter(fileWriter);

            writer.write(word);
            writer.newLine();

            writer.close();
            fileWriter.close();

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    /**
     * Allow the client to choose the desire choose
     *
     * @throws IOException Signals that an I/O exception of some sort has occurred
     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted, either before or during the activity.
     */
    public void menu() throws IOException, InterruptedException {
        int option = 0;
        do{
            showMenu();
            try {
                option = Integer.parseInt(myObj.nextLine());
                System.out.print("\n");
                switch (option) {
                    case 1:
                        changeMaxClients();
                        break;
                    case 2:
                        getMaxNumberClients();
                        break;
                    case 3:
                        addToFilther();
                    case 4:
                        break;
                    case 5:
                        writeMessage();
                        break;
                    case 6:
                        exit();
                        System.out.println(" ----------- Disconnecting Client ----------- ");
                        option = 0;
                        break;
                    default:
                        System.out.println("Please select a valid option");
                }
            }catch(Exception e){
                continue;
            }
        }while (option != 0);

    }

    /**
     * Show the option that the client can choose
     */
    public void showMenu(){
        System.out.println("\n ****************** Client's Menu ****************** \n");
        System.out.println("1. Change Max number of clients. ");
        System.out.println("2. Show current Max clients. ");
        System.out.println("3. Add words to filter. ");
        System.out.println("4. Remove words from filter. ");
        System.out.println("5. Write a message.");
        System.out.println("6. Disconnect from server. ");
        System.out.println("0. Exit. \n");

        System.out.print("  Select an option: ");

    }

    public void run () {
        /*
         https://www.geeksforgeeks.org/producer-consumer-solution-using-threads-java/
        */

        try {
            String mes = "Waiting confirmation from the server";
            while(this.endProcess.availablePermits() != 0) {
                System.out.println(mes);
                String serverResponse = in.readLine();
                if (serverResponse.equals("REQUEST ACCEPTED")){
                    Logger logger = Logger.getLogger("Test");
                    logger.info("The server has accepted your request");
                    activateState();
                }
                while(getClientState().equals(ClientState.OK)){

                    menu();

                }
                mes = "Waiting for the server to accept your request";
            }
        } catch ( IOException | InterruptedException e ) {
            e.printStackTrace ( );
        }

        try {
            socket.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }


}
