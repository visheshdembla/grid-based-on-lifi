import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;

/**
 * Created by Vishesh on 6/1/2017.
 */
public class Main1 {


    public static void main(String args[]) throws IOException, NoSuchPortException, PortInUseException, UnsupportedCommOperationException {
        ConnectionInterface connectionInterface = new ConnectionInterface("COM4");
        DataInputStreamCustom dataInputStreamCustom = new DataInputStreamCustom(connectionInterface.getInputStream(),40);
        System.out.println(dataInputStreamCustom.readInt());

    }
}
