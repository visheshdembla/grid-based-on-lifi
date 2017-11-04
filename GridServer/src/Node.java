import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
  This class is created for clients in the grid

 */
public class Node implements Comparable<Node>{

    private byte connectionMode;

    private long processingCapacity;

    private long storageSpaceRemaining;

    private int connectionID;

    private DataInputStreamCustom dataInputStream;

    private DataOutputStreamCustom dataOutputStream;


    Node (long options[]  , int connectionID ,DataInputStreamCustom dataInputStream ,DataOutputStreamCustom dataOutputStream ){
        this.connectionID = connectionID;
        this.connectionMode =(byte) options[0];
        this.storageSpaceRemaining = options[1];
        this.processingCapacity = options[2];
        this.dataOutputStream = dataOutputStream;
        this.dataInputStream = dataInputStream;
    }


    DataInputStreamCustom getDataInputStream(){
        return this.dataInputStream;
    }

    DataOutputStreamCustom getDataOutputStream(){
        return this.dataOutputStream;
    }


    @Override
    public String toString(){
        return "Node: N-"+this.connectionID;

    }

    long getStorageSpaceRemaining() {
        return storageSpaceRemaining;
    }

    void setStorageSpaceRemaining(long storageSpaceRemaining) {
        this.storageSpaceRemaining = storageSpaceRemaining;
    }

    long getProcessingCapacity(){
        return this.processingCapacity;
    }

    @Override
    public int compareTo(Node node) {
        if(this.storageSpaceRemaining == node.storageSpaceRemaining)
            return 0;
        else if( this.storageSpaceRemaining > node.storageSpaceRemaining)
            return 1;
        else
            return -1;
    }

    public int getConnectionID() {
        return connectionID;
    }

    public void setConnectionID(int connectionID) {
        this.connectionID = connectionID;
    }
}
