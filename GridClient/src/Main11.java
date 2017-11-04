import gnu.io.*;
import jdk.internal.util.xml.impl.Input;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Vishesh on 6/1/2017.
 */
public class Main11 {
        public static void main(String ars[]) throws NoSuchPortException, UnsupportedCommOperationException, IOException, PortInUseException {

            CommPortIdentifier commPortIdentifier = CommPortIdentifier.getPortIdentifier(ServerConfiguration.PORT_NAME);
            if (commPortIdentifier.isCurrentlyOwned()) {
                commPortIdentifier = null;
                return;
            }
            CommPort commPort = commPortIdentifier.open(Main11.class.getName(), 10000);
            if (!(commPort instanceof SerialPort)) {
                commPortIdentifier = null;
                commPort = null;
                return;
            }
            SerialPort serialPort = (SerialPort) commPort;
            serialPort.setSerialPortParams(ServerConfiguration.BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            InputStream inputStream = serialPort.getInputStream();
            OutputStream outputStream = serialPort.getOutputStream();

            DataOutputStreamCustom dataOutputStream = new DataOutputStreamCustom(outputStream, 40);
            DataInputStreamCustom dataInputStream = new DataInputStreamCustom(inputStream, 40);

            dataOutputStream.writeInt(12);


        }
}
