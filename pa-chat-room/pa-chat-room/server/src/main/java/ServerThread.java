import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    private LinkedHashMap<Integer, Socket> waitingList = new LinkedHashMap<>();
    private Semaphore semaphore;
    private Lock controlClients;
    private int maxClients;
    private Lock changeMaxClienstlock;
    private Lock nextClientLock;
    private int currentClients;

    public ServerThread ( int port,  BlockingQueue<String>  bufferUnfilter, BlockingQueue<String>  bufferFilter,Semaphore Write_sem, String filename ) {
        this.port = port;
        this.bufferUnfiltered = bufferUnfilter;
        this.bufferFiltered = bufferFilter;
        this.Write_sem = Write_sem;
        this.maxClients = getMaxClientFromServer(filename);
        this.semaphore = new Semaphore(this.maxClients);
        this.changeMaxClienstlock = new ReentrantLock();
        this.nextClientLock = new ReentrantLock();
        this.controlClients = new ReentrantLock();
        this.currentClients = 0;

        try {
            this.logger = new MyLogger();
            serverSocket = new ServerSocket ( this.port );
        } catch ( IOException e ) {
            e.printStackTrace ( );
        }
    }

    /**
     * See the initial configuration in server.config, where gets the initial number of client allow on the server
     *
     * @param fileName path to the server.config
     * @return inital number of clients that the server allow
     */
    public int getMaxClientFromServer (String fileName){
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

    public void addClient(){
        this.controlClients.lock();
        try{
            this.currentClients++;
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            controlClients.unlock();
        }
    }

    public void removeClient(){
        this.controlClients.lock();
        try{
            this.currentClients--;
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            controlClients.unlock();
        }
    }

    public int getCurrentClients(){
        return this.currentClients;
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }

    /**
     * Allow to know if the server has space for more clients
     *
     * @return true, if it is possible to the client connect to the server
     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted, either before or during the activity.
     */
    public boolean connectClient() throws InterruptedException {
        return semaphore.tryAcquire();
    }

    /**
     * When a client want to disconnect, the server receives a notification. The purpose of this notification is release a space on the server to allow future clients.
     * It's important to have a lock to verificated if two client try to disconnect or edit at the same time.
     */
    public void disconnectClient(){
        this.changeMaxClienstlock.lock();
        try{
            if (getMaxClients() + semaphore.availablePermits() != getMaxClients() || getMaxClients() == getCurrentClients()){
                semaphore.release();
                nextClient();
            }
            removeClient();
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            changeMaxClienstlock.unlock();
        }
    }
    /**
     * Get the number of the client's in the waiting list
     * @return the size of the client's waiting list
     */
    public int getSizeOfClients(){
        return waitingList.size();
    }

    /**
     * Get the actual maximum number of the clients
     * @return the actual maximum number of the clients
     */
    public int getMaxClients(){
        return this.maxClients;
    }

    /**
     * Set the new number of clients that the server allow, this number is inserted for one client
     * @param newNum set the new number of clients that the server allow, this number is inserted for one client
     */
    public void setMaxClients(int newNum){
        this.maxClients = newNum;
    }

    /**
     * Increase a space in the semaphore. This is calculated with the current number - the number inserted from the client
     * @param num set the new number of clients that the server allow, this number is inserted for one client
     */
    public void incrementSem(int num){
        semaphore.release(num);
        int loop = Math.min(waitingList.size(), num);
        for (int i = 0; i < loop; i++) {
            nextClient();
        }
    }

    /**
     * Decrease a space in the semaphore
     * @param num number that decrease the current semaphore
     */
    public void decreaseSem(int num){
        semaphore.tryAcquire(num);
    }

    /**
     * Modify the actual number of the client that the server allow. This is calculated with the current number - the number inserted from the client
     * @param num number inserted from the client to change the actual number
     */
    protected void editMaxNumber(int num){

        this.changeMaxClienstlock.lock();
        try{
            if (num != getMaxClients()){
                int test = num - getMaxClients();
                if (test < 0){
                    int abs = Math.abs(test);
                    decreaseSem(abs);
                }
                else{
                    incrementSem(test);
                }
            }
            setMaxClients(num);
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            changeMaxClienstlock.unlock();
        }
    }

    /**
     * Verify if there are client on the waiting list. If there are, the function activate the client
     */
    private void nextClient(){
        this.nextClientLock.lock();
        try{
            if (!waitingList.isEmpty()){
                Socket socketClient = waitingList.entrySet().iterator().next().getValue();
                int clientId = waitingList.entrySet().iterator().next().getKey();
                new ClientHandler(bufferUnfiltered,bufferFiltered, socketClient, clientId, this).start();
                addClient();
                connectClient();
                waitingList.remove(clientId, socketClient);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            nextClientLock.unlock();
        }
    }

    public void run ( ){

        System.out.println("Server listening...");

        for(int clientId = 0; true; clientId++)
            try {
                Socket clientSocket = this.serverSocket.accept();
                System.out.println("Accepting Data and creating clientSocket");
                boolean available = connectClient();

                logger.logNewMessage(LogType.CONNECTION,clientId,"Hello");

                if (available){
                    System.out.println("Submitting a new clientHandler to the task Queue");
                    new ClientHandler(bufferUnfiltered,bufferFiltered,clientSocket,clientId, this).start();
                    addClient();
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
    private ServerThread server;

    public ClientHandler(BlockingQueue<String> buffer_unfilther,BlockingQueue<String> buffer_filther,Socket clientSocket,Integer clientId, ServerThread server){
        this.buffer_unfilther = buffer_unfilther;
        this.buffer_filther = buffer_filther;
        this.clientSocket = clientSocket;
        this.clientId = clientId;
        this.server = server;

        try {
            this.logger = new MyLogger();
        }
        catch (IOException e)
        {
            System.out.println("Couldn't create logger in ClientHandler");
        }
    }

    /**
     * Server's response to each possible client's action
     *
     * @param  in request from client to server
     * @param  out respond from server to client
     * @throws IOException Signals that an I/O exception of some sort has occurred
     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted, either before or during the activity.
     * @return true to continue the execution of the client handler, false otherwise
     */
    public boolean serverResponse(DataInputStream in, PrintWriter out) throws IOException, InterruptedException {
        String option = in.readUTF();
        switch (option) {
            case "exit" -> {
                //Check if client is still connected.
                // If not, terminate this task to free the thread, so it can answer someone else.
                logger.logNewMessage(LogType.DISCONNECTION, this.clientId, null);
                out.println("SHUTDOWN");
                clientSocket.close();
                server.disconnectClient();
                return false;
            }
            case "get" -> {
                out.println("Current Max number of clients " + server.getMaxClients());
            }
            case "edit" -> {
                out.println("NEW NUMBER");

                String number = in.readUTF();
                server.editMaxNumber(Integer.parseInt(number));
            }
            case "write" -> {
                //Read message from client
                String received_message = in.readUTF();

                //Add message to unfiltered buffer
                buffer_unfilther.add(received_message);
                //Log that we received a message.
                logger.logNewMessage(LogType.MESSAGE, clientId, received_message);

                //Get the censored message.
                String replaced = buffer_filther.take();

                //Write the censored message back to the client.
                out.println(replaced);
                out.flush();
                //Print the censored message to stdout.
                System.out.println(replaced);
            }
        }
        return true;
    }

    public void run(){
        try {
            DataInputStream in = new DataInputStream(this.clientSocket.getInputStream());
            PrintWriter out = new PrintWriter(this.clientSocket.getOutputStream(), true);

            out.println("REQUEST ACCEPTED");

            boolean run = true;

            while(run) {

                run = serverResponse(in, out);

            }
        }
        // Falta ver se o tipo de Exception é java.net.SocketException: Connection reset. Se for , acabar este client.
        // Assim , já não é preciso do isConnected() no inicio do loop.
        catch (Exception e){
            System.out.println(e);
        }
    }
}
