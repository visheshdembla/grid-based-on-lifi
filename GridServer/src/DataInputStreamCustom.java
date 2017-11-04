import java.io.*;
import java.util.logging.Logger;

public class DataInputStreamCustom {

    DataInputStream dataInputStream;
    int defaultCode;
    int connectionID;
    Logger logger = Logger.getLogger(DataInputStreamCustom.class.getName());
    DataInputStreamCustom(InputStream inputStream , int connectionID){
        dataInputStream = new DataInputStream(inputStream);
        this.connectionID = connectionID;
    }

    void close() throws IOException {
        this.dataInputStream.close();
    }

    int read(byte[] b) throws IOException {
        defaultCode = -1;
        while(defaultCode == -1){
            defaultCode = dataInputStream.read();
            logger.warning("df "+defaultCode);
            if(defaultCode != -1)
                break;
        }
        if(dataInputStream.read()==connectionID){
            int len = dataInputStream.read();
            int i;
            for(i = 0 ; i < b.length ; i++){
                b[i] =(byte)dataInputStream.read();
            }

            return i;
        }
        else{
            skip(defaultCode);
            return read(b);
        }
    }

    boolean readBoolean() throws IOException{
        defaultCode = -1;
        while(defaultCode == -1){
            defaultCode = dataInputStream.read();
            if(defaultCode != -1)
                break;
        }
        if(dataInputStream.read()==connectionID){
            return dataInputStream.readBoolean();
        }
        else{
            skip(defaultCode);
            return readBoolean();
        }
    }

    File readFile() throws IOException {
        defaultCode = -1;
        while(defaultCode == -1){
            defaultCode = dataInputStream.read();
            //logger.warning("default code="+defaultCode);
            if(defaultCode != -1)
                break;
        }
        if(dataInputStream.read()==connectionID) {
            int fileSize = dataInputStream.readInt();
            String fileName = dataInputStream.readUTF();
            FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\Vishesh\\Desktop\\" + fileName);
            byte[] fileBuffer = new byte[16 * 1024];
            int read = 0, totalRead = 0,remaining = fileSize;
            while (totalRead != fileSize) {
                while ((read = dataInputStream.read(fileBuffer, 0, Math.min(fileBuffer.length, remaining))) < 0) ;
                totalRead += read;
                remaining -= read;
                fileOutputStream.write(fileBuffer, 0, read);
            }
            fileOutputStream.close();

            return new File("C:\\Users\\Vishesh\\Desktop\\" + fileName);
        }

        else{
            skip(defaultCode);
            return readFile();
        }
    }

    int readInt() throws IOException{
        defaultCode = -1;
        while(defaultCode == -1){
            defaultCode = dataInputStream.read();
            if(defaultCode != -1) {
                logger.warning("df "+defaultCode);
                break;
            }
        }
        if(dataInputStream.read() == connectionID){
            int resp = dataInputStream.readInt();
            return resp;
        }
        else{
            skip(defaultCode);
            return readInt();
        }
    }



    long readLong() throws IOException{
        defaultCode = -1;
        while(defaultCode == -1){
            defaultCode = dataInputStream.read();
            if(defaultCode != -1) {
                break;
            }
        }if(dataInputStream.read()==connectionID){
            long response = dataInputStream.readLong();

            return response;
        }
        else{
            skip(defaultCode);
            return readLong();
        }
    }

    String readUTF() throws IOException {
        defaultCode = -1;
        while(defaultCode == -1){
            defaultCode = dataInputStream.read();
            if(defaultCode != -1) {
                logger.warning("df->"+defaultCode);
                break;
            }
        }
        if(dataInputStream.read() == connectionID){
            String reponse = dataInputStream.readUTF();
            logger.warning("reponse string = "+reponse);
            return reponse;
        }
        else{
            skip(defaultCode);
            return readUTF();
        }
    }

    private void skip(int dataTypeId) throws IOException {
        switch (dataTypeId)
        {
            case 1:
                dataInputStream.readInt();
                break;
            case 2:
                dataInputStream.readLong();
                break;
            case 3:
                dataInputStream.readUTF();
                break;
            case 6:
                dataInputStream.readBoolean();
                break;
            case 9:
                //skip code for readFile
                int len=dataInputStream.readInt();
                dataInputStream.readUTF();
                for(int i=0;i<len;i++){
                    dataInputStream.read();
                }
                break;
            case 10:
                //skip code for read(byte[] b)
                int len1 = dataInputStream.read();
                for(int i = 0 ; i < len1 ; i++){
                    dataInputStream.read();
                }
            default:
                break;

        }
    }
}
