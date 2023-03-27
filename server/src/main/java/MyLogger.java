import java.sql.Timestamp;
import java.util.logging.*;
import java.io.IOException;
import java.util.Date;

/**
 * <p>Keeping a record of all data input, processes, data output, and final results from the clients</p>
 */
 public class  MyLogger {
     Logger logger;

     /**
      * <p>Keeping a record of all data input, processes, data output, and final results from the clients</p>
      * @throws IOException Signals that an I/O exception of some sort has occurred
      */
    public MyLogger() throws IOException {
        this.logger = Logger.getLogger("MyLogger");
    }

     /**
      * <p> This function write the type of action that the client send to the server, each action are specify in LogType </p>
      *
      * @param Type Enum with the differents type of client's action
      * @param clientId Identification number for each client
      * @param message Data that the client send to the server
      *
      */
    public void logNewMessage(LogType Type, int clientId, String message){
        Date date = new Date();
        String msgLog = "";
        if (Type == LogType.CONNECTION){
            msgLog += new Timestamp(date.getTime()) + " - Action : CONNECTED - CLIENT" + clientId;
        } else if (Type == LogType.DISCONNECTION) {
            msgLog += new Timestamp(date.getTime()) + " - Action : DISCONNECTED - CLIENT" + clientId;
        } else if (Type == LogType.WAITING) {
            msgLog += new Timestamp(date.getTime()) + " - Action : WAITING - CLIENT" + clientId;
        } else if (Type == LogType.MESSAGE) {
            msgLog += new Timestamp(date.getTime()) + " - Action : MESSAGE " + "Client - " + clientId + " - " + message;
        } else {
            msgLog += new Timestamp(date.getTime()) + " - Action : ERROR";
        }
        this.logger.info(msgLog);
    }
}

