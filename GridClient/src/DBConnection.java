import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


class DBConnection {
    private Logger logger = Logger.getLogger(DBConnection.class.getName());
    private Connection connection = null;

    DBConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/client", ServerConfiguration.SQL_USERNAME, ServerConfiguration.SQL_PASSWORD
            );
        } catch (Exception e) {
            logger.warning("Problem with SQL Connection");
            e.printStackTrace();
        }
    }

    void insertFileToGrid(String fileName, String fileDigest, long fileSize) {
        String queryFileInsert = "INSERT INTO `file_table`( `file_name`, `file_digest` , `file_size` ) VALUES ('" + fileName + "','" + fileDigest + "','" + fileSize + "')";
        try {
            Statement statement = connection.createStatement();
            statement.execute(queryFileInsert);
        } catch (SQLException e) {
            logger.warning("Could Not Update The DB\n");
        }
    }

    boolean insertFileFromGrid(String fileDigest, int fileFlag) {
        String queryFileInsert = "INSERT INTO `file_table_received`( `file_digest`, `file_flag`) VALUES ('" + fileDigest + "'," + fileFlag + ")";
        try {
            Statement statement = connection.createStatement();
            statement.execute(queryFileInsert);
            return true;
        } catch (SQLException e) {
            logger.warning("Could Not Update The DB");

            return false;
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


    //Change the query to update instead of insert
    void setConnectionID(int connectionID) {
        String sql = "INSERT INTO `connection_table`(`connection_id`) VALUES (\"" + connectionID + "\")";
        try {
            Statement statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            logger.warning(sql);
            logger.warning("could not set connection id");
            e.printStackTrace();
        }
    }

    int getConnectionID() {

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM `connection_table` WHERE 1");
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return ServerConfiguration.DEFAULT_CONNECTION_ID;
            }

        } catch (SQLException e) {
            logger.warning("problem with running sql");
            return ServerConfiguration.DEFAULT_CONNECTION_ID;
        }

    }

    List<String> showStoredFiles() {
        List<String> fileList = new ArrayList<>();
        String queryFileCheck = "SELECT * FROM file_table";
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(queryFileCheck);
            while (rs.next())
                fileList.add(rs.getString("file_name"));
            return fileList;
        } catch (SQLException e) {
            logger.warning("Could Not access The DB");
            return null;
        }
    }

    boolean checkDonatedFile(String fileDigest) {
        String queryFileCheck = "SELECT * FROM file_table_received where file_digest ='" + fileDigest + "' ";
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(queryFileCheck);
            if (rs.next())
                return true;
            else {
                logger.info("File not in database");
                return false;
            }
        } catch (SQLException e) {
            logger.warning("Could Not access The DB");
            return false;
            //e.printStackTrace();
        }
    }


    ResultSet getFileDetails(String fileName) {
        String queryFileCheck = "SELECT * FROM file_table where file_name ='" + fileName + "' ";
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(queryFileCheck);
            if (rs.next())
                return rs;
            else
                return null;

        } catch (SQLException e) {
            logger.warning("Could Not access The DB");
            return null;
        }
    }

}
