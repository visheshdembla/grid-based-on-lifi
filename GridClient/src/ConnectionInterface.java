import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class ConnectionInterface {


    private CommPortIdentifier commPortIdentifier = null;
    private CommPort commPort = null;
    private SerialPort serialPort = null;
    private DataInputStreamCustom dataInputStream = null;
    private DataOutputStreamCustom dataOutputStream = null;
    private int connectionID;
    private boolean connectionStatus = false;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Logger logger = Logger.getLogger(ConnectionInterface.class.getName());
    ConnectionInterface(long[] options) {
        try {
            openSerialPort();
            logger.warning("Port Open");
            dataOutputStream = new DataOutputStreamCustom(outputStream, 40);
            dataInputStream = new DataInputStreamCustom(inputStream, 40);
            connectionID = getConnectionID();
            logger.warning("connection ID = " + connectionID);


            if (this.connectionStatus = connectServer(options[0])) {
                for (long i : options) {
                    logger.warning("option "+i);
                    dataOutputStream.writeLong(i);
                }

                dataInputStream = new DataInputStreamCustom(inputStream, connectionID);
                dataOutputStream = new DataOutputStreamCustom(outputStream, connectionID);
            }
        } catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException | IOException e) {
            e.printStackTrace();
        }
    }

    boolean checkConnectionEstablishment() {
        return this.connectionStatus;
    }


    private int getConnectionID() {

        DBConnection dbConnection = new DBConnection();
        return dbConnection.getConnectionID();
    }

    private void setConnectionID(int connectionID) {
        this.connectionID = connectionID;
        //set connection ID for both dataInputStream and dataOutputStreams
        //also update database

        new DBConnection().setConnectionID(connectionID);

    }

    private boolean connectServer(long mode) throws IOException {
        int connectionMode;
        if (mode == 1)
            connectionMode = ExchangeMessage.CONNECTION_MODE_CLIENT;
        else
            connectionMode = ExchangeMessage.CONNECTION_MODE_DONOR;

        //need for changes here

        dataOutputStream.writeInt(connectionMode);
        dataOutputStream.writeInt(connectionID);

        logger.warning("Connection Mode = "+connectionMode+" connection ID ="+connectionID);
        //need for changes here

        int response = dataInputStream.readInt();
        if (response == ExchangeMessage.UNSUCCESSFUL_CONNECTION_ESTABLISHMENT) {
            logger.warning("UNSUCCESSFUL MESSAGE AAYA");
            return false;
        }
        logger.warning("SUCCESS MESSAGE AAYA");

        if (connectionID == ServerConfiguration.DEFAULT_CONNECTION_ID)
            setConnectionID(dataInputStream.readInt());
        return true;
    }

    private void openSerialPort() throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {

        commPortIdentifier = CommPortIdentifier.getPortIdentifier(ServerConfiguration.PORT_NAME);
        if (commPortIdentifier.isCurrentlyOwned()) {
            commPortIdentifier = null;
            return;
        }
        commPort = commPortIdentifier.open(this.getClass().getName(), 10000);
        if (!(commPort instanceof SerialPort)) {
            commPortIdentifier = null;
            commPort = null;
            return;
        }
        serialPort = (SerialPort) commPort;
        serialPort.setSerialPortParams(ServerConfiguration.BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        inputStream = serialPort.getInputStream();
        outputStream = serialPort.getOutputStream();
    }

    DataInputStreamCustom getDataInputStream() {
        return dataInputStream;
    }

    DataOutputStreamCustom getDataOutputStream() {
        return dataOutputStream;
    }

    void closeConnection() {
        System.exit(0);
    }
}
