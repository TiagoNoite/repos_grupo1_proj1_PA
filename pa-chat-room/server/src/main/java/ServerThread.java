import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class ServerThread extends Thread {

    /* REFERENCES:
    *  https://stackoverflow.com/questions/55985469/limit-number-of-clients-that-will-connect-through-the-server
    * */
    private final int port;
    private final int limitClients;
    private ServerSocket serverSocket;
    BlockingQueue<String> bufferUnfiltered;
    BlockingQueue<String> bufferFiltered;
    Semaphore Write_sem;
    ThreadPoolExecutor executor;
    MyLogger logger;

    public ServerThread ( int port,  BlockingQueue<String>  bufferUnfilter, BlockingQueue<String>  bufferFilter,Semaphore Write_sem,Integer limitClients) {
        this.port = port;
        this.bufferUnfiltered = bufferUnfilter;
        this.bufferFiltered = bufferFilter;
        this.Write_sem=Write_sem;
        this.limitClients = limitClients;

        //this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(limitClients);
        this.executor = new ThreadPoolExecutor(this.limitClients, this.limitClients,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        try {
            this.logger = new MyLogger();
            serverSocket = new ServerSocket ( this.port );
        } catch ( IOException e ) {
            e.printStackTrace ( );
        }
    }


    public void run ( ){

        for(Integer clientId=1;true;clientId++)
            try {

                System.out.println("Accepting Data and creating clientSocket");
                Socket clientSocket = this.serverSocket.accept();

                //logger.logNewMessage(LogType.CONNECTION,clientId,null);
                System.out.println("Submitting a new clientHandler to the task Queue");

                BlockingQueue queue = executor.getQueue();
                if(executor.getActiveCount() >= this.limitClients){
                    logger.logNewMessage(LogType.WAITING, clientId, null);
                }

                executor.submit(new ClientHandler(bufferUnfiltered,bufferFiltered,clientSocket,clientId));
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }


//http://www.cs.sjsu.edu/~pearce/modules/lectures/oop/templates/threads/index.htm


class ClientHandler implements Runnable {
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
            logger.logNewMessage(LogType.CONNECTION,clientId,null);
            DataInputStream in = new DataInputStream(this.clientSocket.getInputStream());
            PrintWriter out = new PrintWriter(this.clientSocket.getOutputStream(), true);

            while(true) {
                //Check if client is still connected.
                // If not, terminate this task to free the thread, so it can answer someone else.
                if (!this.clientSocket.isConnected()) {
                    logger.logNewMessage(LogType.DISCONNECTION,this.clientId,null);
                    this.clientSocket.close();
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
        catch (SocketException se){
            try {
                this.clientSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            logger.logNewMessage(LogType.DISCONNECTION,clientId, null);
        }
        // Falta ver se o tipo de Exception é java.net.SocketException: Connection reset. Se for , acabar este client.
        // Assim , já não é preciso do isConnected() no inicio do loop.
        catch (Exception e){
            System.out.println(e);
        }
    }
}
