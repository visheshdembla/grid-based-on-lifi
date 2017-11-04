/*
 Running jobs in separate thread
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.logging.Logger;

class TaskManager {

    private DBConnection dbConnection = new DBConnection();
    private Logger logger = Logger.getLogger(TaskManager.class.getName());
    private DataInputStreamCustom dataInputStream;
    private DataOutputStreamCustom dataOutputStream;

    public DataInputStreamCustom getDataInputStream() {
        return dataInputStream;
    }

    public DataOutputStreamCustom getDataOutputStream() {
        return dataOutputStream;
    }

    TaskManager(DataInputStreamCustom dataInputStreamCustom, DataOutputStreamCustom dataOutputStreamCustom) {
        this.dataOutputStream = dataOutputStreamCustom;
        this.dataInputStream = dataInputStreamCustom;
    }


    void deleteFile() {
        //Get the File Digest To Be Delete From the Client
        try {
            String fileDigest = dataInputStream.readUTF();
            ResultSet resultSet = dbConnection.getFileDetails(fileDigest);

            if (resultSet == null) {
                dataOutputStream.writeInt(ExchangeMessage.SUCCESSFUL_DELETE_FILE);
                return;
            }

            int destAddress = resultSet.getInt("dest_address");
            logger.warning("dest address "+destAddress);
            Node node = getFileAddressNode(destAddress);

            if (node == null) {
                dataOutputStream.writeInt(ExchangeMessage.DONOR_OFFLINE);
                return;
            }

            DataOutputStreamCustom donorDataOutputStream = node.getDataOutputStream();
            DataInputStreamCustom donorDataInputStream = node.getDataInputStream();

            donorDataOutputStream.writeInt(ExchangeMessage.DELETE_FILE);
            donorDataOutputStream.writeUTF(fileDigest);

            int response = donorDataInputStream.readInt();
            dataOutputStream.writeInt(response);
            if (response == ExchangeMessage.SUCCESSFUL_DELETE_FILE) {
                dbConnection.deleteFileEntryFromGrid(fileDigest, "file_table");
                logger.warning("File " + fileDigest + " deleted");
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
    //DONE
    void storeFile() {
        logger.warning("storing file on grid");
        long fileSize;
        Node donor;
        try {
            fileSize = dataInputStream.readLong();
            logger.warning(" file size " + fileSize);


            donor = getBestFitNode(fileSize);
            if (donor == null) {
                logger.warning("COULD NOT FIND A VALID DONOR");
                dataOutputStream.writeInt(ExchangeMessage.UNSUCCESSFUL_FILE_TRANSFER);
                return;
            }
            logger.warning("Donor ID" + donor.getConnectionID());

            DataOutputStreamCustom donorDataOutputStream = donor.getDataOutputStream();
            DataInputStreamCustom donorDataInputStream = donor.getDataInputStream();
            logger.warning("donor initiated");
            dataOutputStream.writeInt(ExchangeMessage.SUCCESSFUL_FILE_TRANSFER);

            File file = dataInputStream.readFile();


            donorDataOutputStream.writeInt(ExchangeMessage.GET_FILE);
            donorDataOutputStream.writeFile(file);


            int response = donorDataInputStream.readInt();
            dataOutputStream.writeInt(response);


            if (response == ExchangeMessage.SUCCESSFUL_FILE_TRANSFER) {
                donor.setStorageSpaceRemaining(donor.getStorageSpaceRemaining() - fileSize);
                int donorID = donor.getConnectionID();
                dbConnection.insertFile(file.getName(), donorID, fileSize);
                logger.info("FILE " + file.getName() + "STORED TO NODE " + donorID);
            }
            file.delete();
            dataOutputStream.close();
            dataInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //DONE
    void executeCode() {

        Node donor;
        try {


            String fileArguments = dataInputStream.readUTF();
            donor = getLoadBalancedNode();
            if (donor == null) {
                logger.warning("COULD NOT FIND A VALID DONOR NODE");
                dataOutputStream.writeInt(ExchangeMessage.UNSUCCESSFUL_CODE_EXECUTION);
                return;
            }
            DataOutputStreamCustom donorDataOutputStream = donor.getDataOutputStream();
            DataInputStreamCustom donorDataInputStream = donor.getDataInputStream();
            dataOutputStream.writeInt(ExchangeMessage.SUCCESSFUL_CODE_EXECUTION);
            File file = dataInputStream.readFile();
            donorDataOutputStream.writeInt(ExchangeMessage.EXECUTE_CODE);
            donorDataOutputStream.writeUTF(fileArguments);
            donorDataOutputStream.writeFile(file);
            int response = donorDataInputStream.readInt();
            dataOutputStream.writeInt(response);
            dataOutputStream.writeUTF(donorDataInputStream.readUTF());
            if (response == ExchangeMessage.SUCCESSFUL_CODE_EXECUTION) {
                logger.info("CODE EXECUTED");
            } else if (response == ExchangeMessage.UNSUCCESSFUL_CODE_EXECUTION) {
                logger.warning("CODE NOT EXECUTED");
            }
            dataOutputStream.close();
            dataInputStream.close();
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void retrieveFile() {
        try {

            String fileDigest = dataInputStream.readUTF();
            ResultSet resultSet = dbConnection.getFileDetails(fileDigest);

            if (resultSet == null) {
                dataOutputStream.writeInt(ExchangeMessage.RETRIEVE_FILE_PRE_ACK_FAILURE);
                return;
            }

            int destAddress = resultSet.getInt("dest_address");
            long fileSize = resultSet.getLong("file_size");

            Node node = getFileAddressNode(destAddress);
            if (node != null) {
                DataInputStreamCustom donorDataInputStream = node.getDataInputStream();
                DataOutputStreamCustom donorDataOutputStream = node.getDataOutputStream();

                donorDataOutputStream.write(ExchangeMessage.RETRIEVE_FILE);
                donorDataOutputStream.writeUTF(fileDigest);

                //Read Acknowledgement From The Donor Node
                int responseFromDonor = donorDataInputStream.readInt();

                if (responseFromDonor == ExchangeMessage.RETRIEVE_FILE_PRE_ACK_SUCCESS) {
                    File file = donorDataInputStream.readFile();
                    logger.warning("file read");
                    dataOutputStream.writeInt(responseFromDonor);

                    dataOutputStream.writeFile(file);
                    logger.warning("file written");
                } else if (responseFromDonor == ExchangeMessage.RETRIEVE_FILE_PRE_ACK_FAILURE) {
                    dataOutputStream.writeInt(responseFromDonor);
                    dbConnection.deleteFileEntryFromGrid(fileDigest, "file_table");
                }
            } else {
                dataOutputStream.writeInt(ExchangeMessage.DONOR_OFFLINE);
                logger.info("DONOR OFFLINE. TRY AGAIN AFTER SOME TIME");
            }


        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }

    }

    private Node getFileAddressNode(int destAddress) {
        ResourceManager.getNodeArrayList().sort(Node::compareTo);
        for (Node tempNode : ResourceManager.getNodeArrayList()) {
            int tempID = tempNode.getConnectionID();
            if (tempID == destAddress) {
                return tempNode;
            }
        }
        logger.info("DONOR INACTIVE");
        return null;
    }


    private Node getBestFitNode(long requiredSpace) {
        ResourceManager.getNodeArrayList().sort(Node::compareTo);
        for (Node tempNodeForBestFit : ResourceManager.getNodeArrayList()) {
            if ((tempNodeForBestFit.getStorageSpaceRemaining() > requiredSpace))
                return tempNodeForBestFit;
        }
        return null;
    }

    private Node getLoadBalancedNode() {
        Node[] nodes = new Node[ResourceManager.getNodeArrayList().size()];
        ResourceManager.getNodeArrayList().toArray(nodes);
        Node temp = null;
        long max = 0;
        for (Node i : nodes) {
            if (i.getProcessingCapacity() > max) {
                max = i.getProcessingCapacity();
                temp = i;
            }
        }
        return temp;
    }
}
