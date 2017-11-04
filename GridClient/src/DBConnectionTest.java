import static org.junit.Assert.*;
public class DBConnectionTest {
    @org.junit.Test
    public void insertFile() throws Exception {
        DBConnection dbConnection = new DBConnection();
        assertEquals(true,dbConnection.insertFileFromGrid("world",1));
    }





}