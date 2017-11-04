import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

public class ConnectionInterface{

    private final int BAUD_RATE = 3000;
    private OutputStream outputStream;
    private InputStream inputStream;

    public ConnectionInterface(String portName) throws PortInUseException, NoSuchPortException, UnsupportedCommOperationException, IOException {

        CommPortIdentifier commPortIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        Logger logger = Logger.getLogger(ConnectionInterface.class.getName());
        if (commPortIdentifier.isCurrentlyOwned()) {
            logger.warning("Port Is Currently Under Use By Another Process. Cannot Use This Port");
            commPortIdentifier = null;
            return;
        }
        CommPort commPort = commPortIdentifier.open(this.getClass().getName(), 10000);
        if (!(commPort instanceof SerialPort)) {
            logger.warning("The Selected Port is not serial");
            commPortIdentifier = null;
            commPort = null;
            return;
        }
        SerialPort serialPort = (SerialPort) commPort;
        serialPort.setSerialPortParams(BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        inputStream = serialPort.getInputStream();
        outputStream = serialPort.getOutputStream();
    }

    public InputStream getInputStream(){
        return this.inputStream;
    }

    public OutputStream getOutputStream() {
        return this.outputStream;
    }
}
