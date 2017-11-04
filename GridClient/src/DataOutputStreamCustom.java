import java.io.*;
import java.util.logging.Logger;

class DataOutputStreamCustom {
    private DataOutputStream dataOutputStream;
    private int connectionID;
    Logger logger =Logger.getLogger(DataOutputStreamCustom.class.getName());
    DataOutputStreamCustom(OutputStream outputStream , int connectionID){
        dataOutputStream = new DataOutputStream(outputStream);
        this.connectionID =  connectionID;
    }
    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }
    void flush() throws IOException {
        dataOutputStream.flush();
    }
    void close() throws IOException {
        this.dataOutputStream.close();
    }
    int size(){
        return  dataOutputStream.size();
    }


    void write(byte[] b) throws IOException {
        dataOutputStream.write(10);
        dataOutputStream.write(connectionID);
        dataOutputStream.write(b.length);
        dataOutputStream.write(b);
    }
    void write(byte[] b , int off, int len) throws IOException {
        dataOutputStream.write(9);
        dataOutputStream.write(connectionID);
        dataOutputStream.writeInt(off);
        dataOutputStream.writeInt(len);
        dataOutputStream.write(b,off,len);
    }
    void write(int b) throws IOException {
        dataOutputStream.writeInt(connectionID);
        dataOutputStream.write(b);
    }
    void writeBoolean(boolean v) throws IOException {
        dataOutputStream.write(6);
        dataOutputStream.write(connectionID);
        dataOutputStream.writeBoolean(v);
    }
    void writeByte(int v) throws IOException{
        dataOutputStream.write(5);
        dataOutputStream.write(connectionID);
        dataOutputStream.writeByte(v);
    }


    void writeFile(int fileSize , String fileDigest , FileInputStream fileInputStream) throws IOException {
        int count;
        int read = 0, totalRead = 0, remaining = (int)fileSize;
        int bytesRead = 0;
        byte[] fileBuffer = new byte[16*1024];


        dataOutputStream.write(9);
        dataOutputStream.write(connectionID);
        dataOutputStream.writeInt(fileSize);
        dataOutputStream.writeUTF(fileDigest);
        while ((count=fileInputStream.read(fileBuffer)) > 0 && bytesRead!= fileSize) {
            dataOutputStream.write(fileBuffer,0,count);
            bytesRead+=count;
        }
        logger.warning("File sent");
        fileInputStream.close();
    }


    void writeFile(File f) throws IOException {

        FileInputStream fileInputStream = new FileInputStream(f);
        int fileSize = (int)f.length();
        int count;
        int read = 0, totalRead = 0, remaining = (int)fileSize;
        int bytesRead = 0;
        byte[] fileBuffer = new byte[16*1024];


        dataOutputStream.write(9);
        dataOutputStream.write(connectionID);
        dataOutputStream.writeInt(fileSize);
        dataOutputStream.writeUTF(f.getName());
        while ((count=fileInputStream.read(fileBuffer)) > 0 && bytesRead!= fileSize) {
            dataOutputStream.write(fileBuffer,0,count);
            bytesRead+=count;
        }
        logger.warning("File sent");
        fileInputStream.close();
    }

    void writeInt(int v) throws IOException{
        logger.warning("int = "+ v);
        dataOutputStream.write(1);
        dataOutputStream.write(connectionID);
        dataOutputStream.writeInt(v);
    }



    void writeLong(long v) throws IOException{
        dataOutputStream.write(2);
        dataOutputStream.write(connectionID);
        dataOutputStream.writeLong(v);
    }
    void writeUTF(String str) throws IOException{
        dataOutputStream.write(3);
        dataOutputStream.write(connectionID);
        dataOutputStream.writeUTF(str);
    }
}
