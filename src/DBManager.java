import java.nio.file.Paths;
import java.sql.*;
import java.util.TimeZone;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

/**
 * Class for managing DB connections making use of the singleton pattern.
 * Supports any JDBC Connection as long as the proper driver and connection
 * string are provided.
 *
 * @author Nicola Bicocchi
 */
public class DBManager {

    public static final String JDBC_Driver_MySQL = "com.mysql.cj.jdbc.Driver";
    public static final String JDBC_URL_MySQL = "jdbc:mysql://localhost:3306/jdbctest?user=marco&password" +
            "=ciccio2000!&serverTimezone=" + TimeZone.getDefault().getID();

    public static String JDBC_Driver = null;
    public static String JDBC_URL = null;
    static Connection connection;

    private Statement statement;

    public static void setConnection(String Driver, String URL) {
        JDBC_Driver = Driver;
        JDBC_URL = URL;
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null) {
            if (JDBC_Driver == null || JDBC_URL == null) {
                throw new SQLException("Illegal request. Call setConnection() before.");
            }

            try {
                Class.forName(JDBC_Driver);
            } catch (ClassNotFoundException e) {
                throw new SQLException(e.getMessage());
            }

            connection = DriverManager.getConnection(JDBC_URL);
        }
        return connection;
    }

    public static void showMetadata() throws SQLException {
        if (connection == null) {
            throw new SQLException("Illegal request. Connection not established");
        }

        DatabaseMetaData md = connection.getMetaData();
        System.out.println("-- ResultSet Type --");
        System.out.println("Supports TYPE_FORWARD_ONLY: " + md.supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY));
        System.out.println("Supports TYPE_SCROLL_INSENSITIVE: " + md.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE));
        System.out.println("Supports TYPE_SCROLL_SENSITIVE: " + md.supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE));
        System.out.println("-- ResultSet Concurrency --");
        System.out.println("Supports CONCUR_READ_ONLY: " + md.supportsResultSetType(ResultSet.CONCUR_READ_ONLY));
        System.out.println("Supports CONCUR_UPDATABLE: " + md.supportsResultSetType(ResultSet.CONCUR_UPDATABLE));
    }

    public static void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    /**
     * Method used to check both if username and password are correct and to check if the username is available or not
     * * @param username (obligatory)
     * @param password (if null, check if the username is available)
     * @return for option 1 (true = correct values), for option 2 (true = username available)
     */

    public boolean checkUser(String username, String password){
       if(password != null)
       {
           try {
               ResultSet resultSet = statement.executeQuery("select u.username, u.password from Utente u where username='" + username + "' and password ='" + password + "'");
               resultSet.next();
               resultSet.getString("username");
               return true;
           }
           catch (SQLException e)
           {
               return false;
           }
       }

       try {
            ResultSet resultSet = statement.executeQuery("select u.username from Utente u where username='" + username + "'");
            resultSet.next();
            resultSet.getString("username");
            return false;
        }
       catch (SQLException e)
        {
            return true;
        }
    }

    /**
     * Method to add a new user that you want to sign up
     * * @param name (obligatory)
     * @param surname (obligatory)
     * @param email (obligatory)
     * @param username (obligatory)
     * @param password (obligatory)
     * @return true if the insertion was successful, false otherwise
     */

    public boolean addUser(String name, String surname, String email, String username, String password){
        if((name!= null) & (surname != null) & (email!= null) & (username != null) & (password!= null))
        {
            try {
                ResultSet resultSet = statement.executeQuery("insert into 'Utente' ('name', 'surname', 'email', 'username', 'password') values('" + name + "', '" + surname + "', '" + email + "', '" + username + "', '" + password + "')");
                return true;
            }
            catch (SQLException e)
            {
                return false;
            }
        }
        return false;
    }


}