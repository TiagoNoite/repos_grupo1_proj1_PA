import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

public class ServerThread extends Thread {

    /* REFERENCES:
    *  https://stackoverflow.com/questions/55985469/limit-number-of-clients-that-will-connect-through-the-server
    * */
    private final int port;
    private ServerSocket serverSocket;
    BlockingQueue<String> bufferUnfiltered;
    BlockingQueue<String> bufferFiltered;
    Semaphore Write_sem;
    MyLogger logger;
    private HashMap<Integer, Socket> waitingList = new HashMap<>();
    private Semaphore semaphore;

    public ServerThread ( int port,  BlockingQueue<String>  bufferUnfilter, BlockingQueue<String>  bufferFilter,Semaphore Write_sem, String filename ) {
        this.port = port;
        this.bufferUnfiltered = bufferUnfilter;
        this.bufferFiltered = bufferFilter;
        this.Write_sem = Write_sem;
        this.semaphore = new Semaphore(getMaxClient(filename));

        try {
            this.logger = new MyLogger();
            serverSocket = new ServerSocket ( this.port );
        } catch ( IOException e ) {
            e.printStackTrace ( );
        }
    }
    public int getMaxClient (String fileName){
        Properties prop = new Properties(); // creates new property object to read server config file
        try (FileInputStream file = new FileInputStream(fileName)) {
            prop.load(file);
        } catch (FileNotFoundException ex) {
            System.out.println("File does not contain anything"); // FileNotFoundException catch is optional and can be collapsed
        } catch (IOException ex) {
            System.out.println("File does not exist");
        }
        return Integer.parseInt(prop.getProperty("server.max_client"));
    }
    public Semaphore getSemaphore() {
        return semaphore;
    }

    public boolean connectClient(int client) throws InterruptedException {
        return semaphore.tryAcquire();
    }

    public void disconnectClient(int client){
            semaphore.release();
    }

    public int getSizeOfClients(){
        return waitingList.size();
    }

    public void run ( ){

        for(int clientId = 0; true; clientId++)
            try {
                System.out.println("Accepting Data and creating clientSocket");
                boolean available = connectClient(clientId);
                Socket clientSocket = this.serverSocket.accept();
                logger.logNewMessage(LogType.CONNECTION,clientId,"Hello");

                if (available){
                    System.out.println("Submitting a new clientHandler to the task Queue");

                    new ClientHandler(bufferUnfiltered,bufferFiltered,clientSocket,clientId).start();
                    continue;
                }

                waitingList.put(clientId, clientSocket);
                System.out.println("Clients in the queue: " + waitingList.size());

            } catch (IOException | InterruptedException e ) {
                e.printStackTrace();
            }
        }
    }


//http://www.cs.sjsu.edu/~pearce/modules/lectures/oop/templates/threads/index.htm


class ClientHandler extends Thread {
    private BlockingQueue<String> buffer_unfilther;
    private BlockingQueue<String> buffer_filther;
    private Socket clientSocket;
    private MyLogger logger;
    private Integer clientId;

    public ClientHandler(BlockingQueue<String> buffer_unfilther,BlockingQueue<String> buffer_filther,Socket clientSocket,Integer clientId){
        this.buffer_unfilther = buffer_unfilther;
        this.buffer_filther = buffer_filther;
        this.clientSocket = clientSocket;
        this.clientId = clientId;
        try {
            this.logger = new MyLogger();
        }
        catch (IOException e)
        {
            System.out.println("Couldn't create logger in ClientHandler");
        }
    }

    public void run(){
        try {
            DataInputStream in = new DataInputStream(this.clientSocket.getInputStream());
            PrintWriter out = new PrintWriter(this.clientSocket.getOutputStream(), true);

            while(true) {
                //Check if client is still connected.
                // If not, terminate this task to free the thread, so it can answer someone else.
                if (!this.clientSocket.isConnected()) {
                    //Log that client connected.
                    logger.logNewMessage(LogType.DISCONNECTION,this.clientId,null);
                    break;
                }

                //Read message from client
                String received_message = in.readUTF();

                //Add message to unfiltered buffer
                buffer_unfilther.add(received_message);
                //Log that we received a message.
                logger.logNewMessage(LogType.MESSAGE,clientId,received_message);

                //Get the censored message.
                String replaced = buffer_filther.take();

                //Write the censored message back to the client.
                out.println(replaced);
                out.flush();
                //Print the censored message to stdout.
                System.out.println(replaced);
            }
        }
        // Falta ver se o tipo de Exception é java.net.SocketException: Connection reset. Se for , acabar este client.
        // Assim , já não é preciso do isConnected() no inicio do loop.
        catch (Exception e){
            System.out.println(e);
        }
    }
}
