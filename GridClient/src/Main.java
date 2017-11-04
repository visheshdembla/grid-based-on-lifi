//Main Class Of The Grid Client Side Code
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class Main {
    public static void main(String args[]) {
        Logger logger = Logger.getLogger(Main.class.getName());
        ConnectionInterface connectionInterface;
        Scanner scanner = new Scanner(System.in);
        long availableSpace;
        File file;
        System.out.println("\n\t\t\tGRID CLIENT INTERFACE\nPLEASE ENTER THE CHOICE:\n1:ESTABLISH CONNECTION\n2:QUIT");
        switch (scanner.nextInt()) {
            case 1:
                long options[] = new long[3];
                System.out.println("Choose the mode for the connection:\n1:Service requester mode\n2:Resource donor mode");
                options[0] = scanner.nextLong();
                if (options[0] == 2) {
                    System.out.println("Choose Options for donation(1:YES\t0:NO)\na:Donate Disk Space\nb:Donate Computation Power");
                    options[1] = scanner.nextLong();
                    options[2] = scanner.nextLong();
                }

                if (options[1] == 1) {
                    availableSpace = new File("/").getFreeSpace() / 10240;
                    options[1] = availableSpace;
                }

                if (options[2] == 1) {
                    try {
                        options[2] = new Sigar().getCpu().getTotal();
                    } catch (SigarException e) {
                        e.printStackTrace();
                    }
                }

                connectionInterface = new ConnectionInterface(options);
                if (connectionInterface.checkConnectionEstablishment())
                    System.out.println("CONNECTION ESTABLISHED!");
                else {
                    System.out.println("ERROR! UNABLE TO ESTABLISH CONNECTION!");
                    connectionInterface.closeConnection();
                }

                if (options[0] == 1) {
                    FunctionRequester functionRequester = new FunctionRequester(connectionInterface.getDataInputStream(), connectionInterface.getDataOutputStream());
                    byte choice;
                    System.out.println("CHOOSE A JOB TYPE:\n1: STORE FILE\n2: RETRIEVE FILE  \n3: DELETE FILE \n4:REMOTE PROGRAM EXECUTION \n5: EXIT PROGRAM");
                    choice = scanner.nextByte();
                    List<String> fileList;
                    switch (choice) {

                        case 1:
                            System.out.println("Enter the path for the file to be stored:");
                            scanner.nextLine();
                            file = new File(scanner.nextLine());
                            if (file.isFile()) {
                                logger.warning("store file");
                                functionRequester.storeFileOnGrid(file);
                                //Update code to not close connection here
                                connectionInterface.closeConnection();
                            } else {
                                try {
                                    //Replace 0 with macro message
                                    connectionInterface.getDataOutputStream().writeInt(0);
                                    System.out.println("THIS FILE DOES NOT EXIST");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;


                        case 2:

                            fileList = new DBConnection().showStoredFiles();
                            for (String i : fileList) {
                                System.out.println(fileList.indexOf(i) + 1 + ". " + i);
                            }

                            if(fileList.size()>0) {
                                System.out.println("Enter the File number of the file to be retrieved");
                                try {
                                    functionRequester.retrieveFileFromGrid(fileList.get(scanner.nextInt() - 1));
                                    connectionInterface.closeConnection();
                                } catch (SQLException | IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else{
                                System.out.println("NO FILE STORED ON THE GRID");
                                try {
                                    connectionInterface.getDataOutputStream().writeInt(ExchangeMessage.LOG_OUT);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;

                        case 3:

                            fileList = new DBConnection().showStoredFiles();
                            for (String i : fileList) {
                                System.out.println(fileList.indexOf(i) + 1 + ". " + i);
                            }
                            if(fileList.size() > 0) {
                                System.out.println("Enter the File number of the file to be deleted");
                                functionRequester.deleteFileOnGrid(fileList.get(scanner.nextInt() - 1));
                        //        connectionInterface.closeConnection();
                            }
                            else{
                                System.out.println("NO FILE STORED ON THE GRID");
                                try {
                                    connectionInterface.getDataOutputStream().writeInt(ExchangeMessage.LOG_OUT);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            break;

                        case 4:
                            System.out.println("Enter the path for the JAVA CODE FILE that you want to execute");
                            scanner.nextLine();
                            String fileName = scanner.next();
                            String fileArguments = scanner.nextLine();
                            file = new File(fileName);
                            if (file.isFile()) {
                                functionRequester.executeCodeOnGrid(file, fileArguments);
                                connectionInterface.closeConnection();
                            } else {
                                try {
                                    connectionInterface.getDataOutputStream().writeInt(0);
                                    System.out.println("THIS FILE DOES NOT EXIST");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;


                        case 5:
                            try {
                                connectionInterface.getDataOutputStream().writeInt(ExchangeMessage.LOG_OUT);
                                connectionInterface.closeConnection();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            System.out.println("YOU HAVE LOGGED OUT!");
                            break;

                        default:
                            connectionInterface.closeConnection();
                            System.out.println("INVALID CHOICE");
                            break;
                    }
                }

                //donor code
                else {
                    //logger.info("DONOR ENABLED");
                    FunctionDonor functionDonor = new FunctionDonor(connectionInterface.getDataInputStream(), connectionInterface.getDataOutputStream());
                    if (options[1] != 0)
                        functionDonor.createDonorFolder();
                    int operation = 0;
                    while (true) {
                        try {
                            operation = connectionInterface.getDataInputStream().readInt();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        switch (operation) {
                            case ExchangeMessage.GET_FILE:
                                functionDonor.storeFileFromGrid();
                                break;
                            case ExchangeMessage.EXECUTE_CODE:
                                functionDonor.executeCodeFromGrid();
                                break;
                            case ExchangeMessage.RETRIEVE_FILE:
                                functionDonor.retrieveFileToGrid();
                                break;
                            case ExchangeMessage.DELETE_FILE:
                                functionDonor.deleteFileFromGrid();
                                break;
                            case ExchangeMessage.LOG_OUT:
                                connectionInterface.closeConnection();
                                break;
                            default:
                                logger.info("INVALID OPERATION DETECTED " + operation);
                                break;
                        }
                    }
                }
                break;
            default:
                System.out.print("GOODBYE");
                break;
        }
    }
}