import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>Class ServerThread handle every server's interaction with the clients</p>
 *
 */
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
    private int maxClients;
    private int currentClients;
    private Lock changeMaxClienstlock;
    private Lock nextClientLock;
    private Lock controlClients;
    private Lock controlFileAccess;

    /**
     * <p>Class ServerThread's Constructor</p>
     *
     * @param port Port where the server can connect
     * @param bufferFilter List of messages that have already passed from the filter
     * @param bufferUnfilter List of messages that haven't passed from the filter
     * @param Write_sem control
     * @param filename path where the server can access to the data
     *
     */
    public ServerThread(int port, BlockingQueue<String> bufferUnfilter, BlockingQueue<String> bufferFilter, Semaphore Write_sem, String filename) {
        this.port = port;
        this.bufferUnfiltered = bufferUnfilter;
        this.bufferFiltered = bufferFilter;
        this.Write_sem = Write_sem;
        this.maxClients = getMaxClientFromServer(filename);
        this.semaphore = new Semaphore(this.maxClients);
        this.changeMaxClienstlock = new ReentrantLock();
        this.nextClientLock = new ReentrantLock();
        this.controlClients = new ReentrantLock();
        this.controlFileAccess = new ReentrantLock();
        this.currentClients = 0;

        try {
            this.logger = new MyLogger();
            serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>See the initial configuration in server.config, where gets the initial number of client allow on the server</p>
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

    /**
     * <p>Increase the number of client each time that one client want to join</p>
     */
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

    /**
     * <p>Decrease the number of client each time that one client want to leave</p>
     */
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

    /**
     * <p>Get the number of the client's in the waiting list</p>
     *
     * @return the size of the client's waiting list
     */
    public int getCurrentClients(){
        return this.currentClients;
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }

    /**
     * <p>Allow to know if the server has space for more clients</p>
     *
     * @return true, if it is possible to the client connect to the server
     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted, either before or during the activity.
     */
    public boolean connectClient() throws InterruptedException {
        return semaphore.tryAcquire();
    }

    /**
     * <p>When a client want to disconnect, the server receives a notification. The purpose of this notification is release a space on the server to allow future clients.
     * It's important to have a lock to verificated if two client try to disconnect or edit at the same time.</p>
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
     * <p>Get the actual maximum number of the clients</p>
     * @return the actual maximum number of the clients
     */
    public int getMaxClients(){
        return this.maxClients;
    }

    /**
     * <p>Set the new number of clients that the server allow, this number is inserted for one client</p>
     * @param newNum set the new number of clients that the server allow, this number is inserted for one client
     */
    public void setMaxClients(int newNum){
        this.maxClients = newNum;
    }

    /**
     * <p>Increase a space in the semaphore. This is calculated with the current number - the number inserted from the client</p>
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
     * <p>Decrease a space in the semaphore</p>
     * @param num number that decrease the current semaphore
     */
    public void decreaseSem(int num){
        semaphore.tryAcquire(num);
    }

    /**
     * <p>Modify the actual number of the client that the server allow. This is calculated with the current number - the number inserted from the client</p>
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
     * <p>Verify if there are client on the waiting list. If there are, the function activate the client</p>
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

    /**
     * <p>Add word to the filter, these words are writing by clients</p>
     * @param word word given by the client to add to the filter
     */
    public void addWordToFilter(String word) {
        this.controlFileAccess.lock();
        try {
            FileWriter fileWriter = new FileWriter("./filter.txt", true);
            BufferedWriter writer = new BufferedWriter(fileWriter);

            writer.newLine();
            writer.write(word);

            writer.close();
            fileWriter.close();

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        finally {
            controlFileAccess.unlock();
        }
    }

    /**
     * <p>Remove client's messages from the filter</p>
     * @param word word given by the client to remove from the filter
     */
    public void removeWordFromFilter(String word) {
        this.controlFileAccess.lock();
        try {
            Scanner sc = new Scanner(new File("./filter.txt"));
            StringBuilder sb = new StringBuilder();
            String first = sc.nextLine();

            if (first != null){
                sb.append(first);
            }

            while (sc.hasNextLine()) {
                String input = sc.nextLine();
                if (!input.strip().equalsIgnoreCase(word)){
                    sb.append("\n").append(input);
                }
            }

            PrintWriter writer = new PrintWriter("./filter.txt");
            writer.append(sb.toString());
            writer.flush();

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        finally {
            controlFileAccess.unlock();
        }
    }

    /**
     * <p>Show words at currently in the filter</p>
     */
    public String showWords(){
        StringBuilder words = new StringBuilder();
        this.controlFileAccess.lock();
        try {
            int count = 1;

            BufferedReader br = new BufferedReader(new FileReader("./filter.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                words.append(line).append("   ");
                if (count%5 == 0){
                    words.append("\n");
                }
                count++;
            }

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        finally {
            controlFileAccess.unlock();
        }
        return words.append("\n").append("FINISHED READING A FILE").toString();
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
                logger.logNewMessage(LogType.WAITING, clientId, null);
                System.out.println("Clients in the queue: " + waitingList.size());

            } catch (IOException | InterruptedException e ) {
                e.printStackTrace();
            }
        }
    }


//http://www.cs.sjsu.edu/~pearce/modules/lectures/oop/templates/threads/index.htm

/**
 * <p>See the initial configuration in server.config, where gets the initial number of client allow on the server</p>
 */
class ClientHandler extends Thread {
    private BlockingQueue<String> buffer_unfilther;
    private BlockingQueue<String> buffer_filther;
    private Socket clientSocket;
    private MyLogger logger;
    private Integer clientId;
    private ServerThread server;

    /**
     * <p>Class ClientHandler's Constructor</p>
     *
     * @param buffer_filther List of messages that have already passed from the filter
     * @param buffer_unfilther List of messages that haven't passed from the filter
     * @param clientSocket Port where the server can connect
     * @param clientId Number of identification from each client
     * @param server Sever where the clients are
     *
     */
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
     * <p>Server's response to each possible client's action</p>
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
            case "add" -> {
                String received_word = in.readUTF();
                server.addWordToFilter(received_word);
            }
            case "remove" -> {
                String received_word = in.readUTF();
                server.removeWordFromFilter(received_word);
            }
            case "show" -> {
                out.println(server.showWords());
            }
            default -> {}
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
        catch (Exception e){
            System.out.println(e);
            logger.logNewMessage(LogType.DISCONNECTION, this.clientId, null);
            try {
                this.clientSocket.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            server.disconnectClient();
        }
    }
}
