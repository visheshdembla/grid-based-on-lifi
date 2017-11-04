//ALL DONOR RELATED TASKS;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

class FunctionDonor {
    private DataInputStreamCustom dataInputStream;
    private DataOutputStreamCustom dataOutputStream;
    private DBConnection dbConnection;
    private Logger logger;
    private String gridDirectoryPath = "C:\\Users\\" + System.getProperty("user.name") + "\\Desktop\\GRID_FOLDER\\";

    FunctionDonor(DataInputStreamCustom dataInputStream, DataOutputStreamCustom dataOutputStream) {
        this.dbConnection = new DBConnection();
        this.logger = Logger.getLogger(FunctionRequester.class.getName());
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
    }

    void createDonorFolder() {
        File file = new File("C:\\Users\\" + System.getProperty("user.name") + "\\Desktop\\GRID_FOLDER\\");
        if (!file.exists()) {
            if (file.mkdir())
                logger.info("DIRECTORY CREATED");
            else
                logger.info("UNABLE TO CREATE DIRECTORY FOR GRID");
        }
    }

    private String getMessageDigest(File file) {
        MessageDigest messageDigest = null;
        FileInputStream fileInputStream;
        byte[] dataBytes = new byte[16384];
        int nread;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            fileInputStream = new FileInputStream(file);
            while ((nread = fileInputStream.read(dataBytes)) != -1) {
                messageDigest.update(dataBytes, 0, nread);
            }
            fileInputStream.close();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }


        byte[] mdbytes = new byte[0];
        if (messageDigest != null) {
            mdbytes = messageDigest.digest();
        }
        //convert the byte to hex
        StringBuilder hexString = new StringBuilder();
        for (byte mdbyte : mdbytes) {
            String hex = Integer.toHexString(0xff & mdbyte);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    //DONE
    void storeFileFromGrid() {
        try {
            File file = dataInputStream.readFile();
            String fileDigest = file.getName();
            logger.warning("file data received");
            if (fileDigest.equals(getMessageDigest(file))) {
                logger.info("FILE STORED LOCALLY");
                dbConnection.insertFileFromGrid(fileDigest, 1);
                dataOutputStream.writeInt(ExchangeMessage.SUCCESSFUL_FILE_TRANSFER);
            }
            else {
                logger.info("FILE RECEIVED IS INVALID");
                //dbConnection.insertFileFromGrid(fileDigest, 0);
                file.delete();
                dataOutputStream.writeInt(ExchangeMessage.UNSUCCESSFUL_FILE_TRANSFER);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //DONE
    void executeCodeFromGrid() {
        try {
            String fileArguments = dataInputStream.readUTF();
            File file = dataInputStream.readFile();
            String fileName = file.getName();
            Runtime runtime = Runtime.getRuntime();
            String command = "cmd.exe /c cd " + gridDirectoryPath + " && cmd.exe /c javac " + fileName + " && cmd.exe /c java" +
                    " " + fileName.substring(0, fileName.indexOf(".")) + " " + fileArguments;
            Process process = runtime.exec(command);
            process.waitFor();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            StringBuilder stringBuffer = new StringBuilder();
            StringBuilder stringBuffer1 = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line).append("\n");
            }
            while ((line = bufferedReader1.readLine()) != null) {
                stringBuffer1.append(line).append("\n");
            }
            if (stringBuffer.toString().equals("")) {
                dataOutputStream.writeInt(ExchangeMessage.UNSUCCESSFUL_CODE_EXECUTION);
                dataOutputStream.writeUTF(stringBuffer1.toString());
            } else {
                dataOutputStream.writeInt(ExchangeMessage.SUCCESSFUL_CODE_EXECUTION);
                dataOutputStream.writeUTF(stringBuffer.toString());
            }

            file.delete();
            new File(gridDirectoryPath+fileName.substring(0, fileName.indexOf("."))+".class").delete();


        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            try {
                dataOutputStream.writeInt(ExchangeMessage.UNSUCCESSFUL_CODE_EXECUTION);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    void retrieveFileToGrid() {
        try {

            String fileDigest = dataInputStream.readUTF();
            if (dbConnection.checkDonatedFile(fileDigest)) {
                File file = new File(gridDirectoryPath + fileDigest);
                if (!file.isFile()) {
                    dataOutputStream.writeInt(ExchangeMessage.RETRIEVE_FILE_PRE_ACK_FAILURE);
                    dbConnection.deleteFileEntryFromGrid(fileDigest, "file_table_received");
                    return;
                }

                dataOutputStream.writeInt(ExchangeMessage.RETRIEVE_FILE_PRE_ACK_SUCCESS);
                dataOutputStream.writeFile(file);

            } else {
                dataOutputStream.writeInt(ExchangeMessage.RETRIEVE_FILE_PRE_ACK_FAILURE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void deleteFileFromGrid() {
        try {
            String fileDigest = dataInputStream.readUTF();
            if (dbConnection.checkDonatedFile(fileDigest)) {
                logger.warning("file is present here");
                String path = gridDirectoryPath + fileDigest;
                logger.warning("file path = "+path);
                if (new File(path).delete()) {
                    dbConnection.deleteFileEntryFromGrid(fileDigest, "file_table_received");
                    dataOutputStream.writeInt(ExchangeMessage.SUCCESSFUL_DELETE_FILE);
                    logger.warning("file deleted");
                } else {
                    dataOutputStream.writeInt(ExchangeMessage.UNSUCCESSFUL_DELETE_FILE);
                }
            }
            else{
                dataOutputStream.writeInt(ExchangeMessage.SUCCESSFUL_DELETE_FILE);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
