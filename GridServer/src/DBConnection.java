import java.sql.*;
import java.util.logging.Logger;

class DBConnection {
    private Logger logger = Logger.getLogger(DBConnection.class.getName());
    private Connection connection = null;

    DBConnection(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String password = "";
            String username = "root";
            this.connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/server", username, password
            );
        } catch (Exception e) {
            logger.warning("Problem with SQL Connection");
            e.printStackTrace();
        }
    }

    int getNewConnectionID(){
        String querySelect = "SELECT * FROM client_id_table";

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(querySelect);

            while(resultSet.next()){
                return resultSet.getInt("client_id_count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    void incrementConnectionID(){
        int connectionID = getNewConnectionID();
        connectionID++;
        String updateQuery = "UPDATE `client_id_table` SET `client_id_count`="+connectionID+" WHERE 1";

        try {
            Statement statement = connection.createStatement();
            statement.execute(updateQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void insertFile( String fileDigest, int destAddress , long fileSize){
        String queryFileInsert = "INSERT INTO `file_table`(`file_digest` , `dest_address` , `file_size` ) VALUES ('"+fileDigest+"','"+destAddress+"' , '"+fileSize+"')";
        try {
            Statement statement = connection.createStatement();
            statement.execute(queryFileInsert);
            logger.info("file inserted into database");
        } catch (SQLException e) {
            logger.info("could not insert file into the database");
            //e.printStackTrace();
        }
    }



    ResultSet getFileDetails(String fileDigest){
        String queryFileInsert = "SELECT * FROM file_table WHERE file_digest = '"+fileDigest+"'";

        try {
            Statement statement = connection.createStatement();
            ResultSet rs =statement.executeQuery(queryFileInsert);
            if(rs.next()){
                return rs;
            }
            else return null;
        } catch (SQLException e) {
            return null;
            //e.printStackTrace();
        }
    }


    void deleteFileEntryFromGrid(String fileDigest , String tableName){
        String queryFileDelete = "DELETE FROM `"+tableName+"` WHERE `file_digest` = '"+fileDigest+"'";
        try {
            Statement statement = connection.createStatement();
            statement.execute(queryFileDelete);
        } catch (SQLException e) {
            logger.info("Could Not Delete From Database");
        }
    }


}
