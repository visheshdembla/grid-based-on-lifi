import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.util.logging.Logger;

public class Server {
    private static String portName = "COM4";
    public static void main(String ags[]) throws PortInUseException, IOException, NoSuchPortException, UnsupportedCommOperationException {
        ConnectionInterface connectionInterface = new ConnectionInterface(portName);
        DataInputStreamCustom dataInputStream = new DataInputStreamCustom(connectionInterface.getInputStream(),40);
        DataOutputStreamCustom dataOutputStream = new DataOutputStreamCustom(connectionInterface.getOutputStream(),40);
        Logger logger = Logger.getLogger(Server.class.getName());
        while(true){
            logger.warning("Accpting Connections");

            int response = dataInputStream.readInt();
            logger.warning("response "+response);

            if(response == ExchangeMessage.CONNECTION_MODE_CLIENT || response == ExchangeMessage.CONNECTION_MODE_DONOR){
                int clientID = dataInputStream.readInt();

                logger.warning("ID retrieved "+clientID);

                if(clientID == ExchangeMessage.DEFAULT_CONNECTION_ID){
                    //assign new ID and send new ID
                    DBConnection dbConnection = new DBConnection();
                    clientID = dbConnection.getNewConnectionID();
                    clientID++;

                    if(clientID == 0){
                        dataOutputStream.writeInt(ExchangeMessage.UNSUCCESSFUL_CONNECTION_ESTABLISHMENT);
                        continue;
                    }
                    dataOutputStream.writeInt(ExchangeMessage.SUCCESSFUL_CONNECTION_ESTABLISHMENT);
                    dataOutputStream.writeInt(clientID);
                    dbConnection.incrementConnectionID();
                    logger.warning("new id assigned "+clientID+1);
                }
                else{
                    logger.warning("Success Message Likha");
                    dataOutputStream.writeInt(ExchangeMessage.SUCCESSFUL_CONNECTION_ESTABLISHMENT);
                }

                long [] options = new long[3];
                for(int i = 0 ; i < 3 ; i++) {
                    options[i] = dataInputStream.readLong();
                }
                logger.warning("option read");


                if(response == ExchangeMessage.CONNECTION_MODE_DONOR){
                    ResourceManager.addNode(new Node(options , clientID ,new DataInputStreamCustom(connectionInterface.getInputStream(),clientID) , new DataOutputStreamCustom(connectionInterface.getOutputStream() , clientID)));
                    logger.warning("donor "+ clientID+" added");
                }
                else {
                    TaskManager taskManager = new TaskManager(new DataInputStreamCustom(connectionInterface.getInputStream(),clientID),new DataOutputStreamCustom(connectionInterface.getOutputStream(),clientID));
                    logger.warning("client connected");
                    int choice = taskManager.getDataInputStream().readInt();
                    logger.warning("choice = "+choice);
                    switch (choice) {
                        case ExchangeMessage.SEND_FILE:
                            taskManager.storeFile();
                            break;

                        case ExchangeMessage.EXECUTE_CODE:
                            taskManager.executeCode();
                            break;

                        case ExchangeMessage.RETRIEVE_FILE:
                            taskManager.retrieveFile();
                            break;

                        case ExchangeMessage.DELETE_FILE:
                            taskManager.deleteFile();
                            break;

                        case ExchangeMessage.LOG_OUT:
                            //execute logout
                            logger.warning("client disconnected");
                            break;
                        default:
                            //execute invalid command
                            break;
                    }
                }
            }
        }




    }
}
