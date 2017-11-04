//ALL REQUESTER RELATED TASKS

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

class FunctionRequester {
    private DataInputStreamCustom dataInputStream;
    private DataOutputStreamCustom dataOutputStream;
    private DBConnection dbConnection;
    private Logger logger;
    private String downloadDirectoryPath = "C:\\Users\\" + System.getProperty("user.name") + "\\Downloads\\";

    FunctionRequester(DataInputStreamCustom dataInputStream, DataOutputStreamCustom dataOutputStream) {
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.dbConnection = new DBConnection();
        this.logger = Logger.getLogger(FunctionRequester.class.getName());
    }

    void deleteFileOnGrid(String fileName) {
        try {
            ResultSet resultSet = dbConnection.getFileDetails(fileName);
            String fileDigest = resultSet.getString("file_digest");
            dataOutputStream.writeInt(ExchangeMessage.DELETE_FILE);
            dataOutputStream.writeUTF(fileDigest);
            int response = dataInputStream.readInt();

            if (response == ExchangeMessage.SUCCESSFUL_DELETE_FILE) {
                dbConnection.deleteFileEntryFromGrid(fileDigest, "file_table");
                System.out.println("FILE " + fileName + " deleted.");
            } else if (response == ExchangeMessage.UNSUCCESSFUL_DELETE_FILE) {
                logger.warning("COULD NOT PERFORM DELETE OPERATION");
            } else if (response == ExchangeMessage.DONOR_OFFLINE) {
                logger.warning("NODE IS OFFLINE");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }


    }

    static String getMessageDigest(File file) {
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
    void storeFileOnGrid(File file) {
        String fileName = file.getName();
        long fileSize = file.length();
        String fileDigest = getMessageDigest(file);
        try {
            FileInputStream fileInputStream = new FileInputStream(file);


            dataOutputStream.writeInt(ExchangeMessage.SEND_FILE);
            dataOutputStream.writeLong(fileSize);
            logger.warning("meta data sent");


            if (dataInputStream.readInt() == ExchangeMessage.UNSUCCESSFUL_FILE_TRANSFER) {
                System.out.println("Valid Donor Not Found! Please Try Again Later");
                return;
            }


            dataOutputStream.writeFile((int) fileSize, fileDigest, fileInputStream);

            logger.warning("file data sent");


            int response = dataInputStream.readInt();

            if (response == ExchangeMessage.SUCCESSFUL_FILE_TRANSFER) {
                logger.info("File Transfer Was Successful");
                dbConnection.insertFileToGrid(fileName, fileDigest, fileSize);
            } else if (response == ExchangeMessage.UNSUCCESSFUL_FILE_TRANSFER) {
                System.out.println("File Transfer Was Not Successful");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //DONE
    void executeCodeOnGrid(File file, String fileArguments) {
        if (!file.getName().endsWith("java")) {
            logger.warning("FiLE TYPE WAS NOT JAVA.");
            return;
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            dataOutputStream.writeInt(ExchangeMessage.EXECUTE_CODE);
            dataOutputStream.writeUTF(fileArguments);
            if (dataInputStream.readInt() == ExchangeMessage.UNSUCCESSFUL_CODE_EXECUTION) {
                System.out.println("No Valid Node Found! Please try again");
                return;
            }
            dataOutputStream.writeFile(file);


            int response = dataInputStream.readInt();
            String output = dataInputStream.readUTF();
            if (response == ExchangeMessage.SUCCESSFUL_CODE_EXECUTION) {
                logger.warning("SUCCESSFUL CODE EXECUTION. OUTPUT IS:\n");
            } else if (response == ExchangeMessage.UNSUCCESSFUL_CODE_EXECUTION) {
                logger.warning("UNSUCCESSFUL CODE EXECUTION.ERROR IS\n");
            }
            System.out.println(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void retrieveFileFromGrid(String fileName) throws SQLException, IOException {
        ResultSet resultSet = dbConnection.getFileDetails(fileName);
        String fileDigest = resultSet.getString("file_digest");
        long fileSize = resultSet.getLong("file_size");

        if (fileDigest != null && fileSize != -1) {


            dataOutputStream.writeInt(ExchangeMessage.RETRIEVE_FILE);
            dataOutputStream.writeUTF(fileDigest);

            int responseFromServer = dataInputStream.readInt();
            if (responseFromServer == ExchangeMessage.RETRIEVE_FILE_PRE_ACK_SUCCESS) {

                File file = dataInputStream.readFile(fileName);

            } else if (responseFromServer == ExchangeMessage.RETRIEVE_FILE_PRE_ACK_FAILURE) {
                System.out.println("FILE NOT FOUND!");
                dbConnection.deleteFileEntryFromGrid(fileDigest, "file_table");
            } else if (responseFromServer == ExchangeMessage.DONOR_OFFLINE) {
                System.out.println("NODE WITH THE FILE IS OFFLINE. TRY AGAIN LATER.");
            }
        } else {
            logger.info("ERROR WITH DATABASE! COULD NOT RETRIEVE THE REQUESTED FILE");
        }
    }

}
