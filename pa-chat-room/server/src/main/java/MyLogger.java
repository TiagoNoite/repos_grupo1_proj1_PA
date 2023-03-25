import java.sql.Timestamp;
import java.util.logging.*;
import java.io.IOException;
import java.util.Date;

 public class  MyLogger {
     Logger logger;

    public MyLogger() throws IOException {
        this.logger = Logger.getLogger("MyLogger");
    }

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

