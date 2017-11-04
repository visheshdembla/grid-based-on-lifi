/**
 * Created by vishesh on 20/3/17.
 */
public class ExchangeMessage {
    static final int ESTABLISH_CONNECTION_NEW = 1;
    static final int ESTABLISH_CONNECTION_EXISTING = 2;
    static final int SUCCESSFUL_CONNECTION_ESTABLISHMENT = 3;
    static final int UNSUCCESSFUL_CONNECTION_ESTABLISHMENT = 4;
    static final int SEND_FILE = 5;
    static final int EXECUTE_CODE = 6;
    static final int LOG_OUT = 7;
    static final int GET_FILE = 8;
    static final int SUCCESSFUL_FILE_TRANSFER = 9;
    static final int UNSUCCESSFUL_FILE_TRANSFER = 10;
    static final int SUCCESSFUL_CODE_EXECUTION = 11;
    static final int UNSUCCESSFUL_CODE_EXECUTION = 12;
    static final int RETRIEVE_FILE = 13;
    static final int RETRIEVE_FILE_PRE_ACK_SUCCESS = 14;
    static final int SUCCESSFUL_FILE_RETRIEVE= 15;
    static final int UNSUCCESSFUL_FILE_RETRIEVE= 16;
    static final int RETRIEVE_FILE_PRE_ACK_FAILURE = 17;
    static final int DONOR_OFFLINE = 18;
    static final int DELETE_FILE = 19;
    static final int SUCCESSFUL_DELETE_FILE =20;
    static final int UNSUCCESSFUL_DELETE_FILE = 21;
    //
    static final int ACCEPT_CONNECTION_MODE = 22;
    static final int CONNECTION_MODE_CLIENT = 23;
    static final int CONNECTION_MODE_DONOR = 24;

}