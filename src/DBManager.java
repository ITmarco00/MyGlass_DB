import org.json.JSONArray;

import java.nio.file.Paths;
import java.sql.*;
import java.util.TimeZone;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.json.CDL.rowToString;

/**
 * Class for managing DB connections making use of the singleton pattern.
 * Supports any JDBC Connection as long as the proper driver and connection
 * string are provided.
 *
 * @author Nicola Bicocchi
 */
public class DBManager {

    public static final String JDBC_Driver_MySQL = "com.mysql.cj.jdbc.Driver";
    public static final String JDBC_URL_MySQL = "jdbc:mysql://localhost:3306/myglassdb?user=marco&password" +
            "=ciccio2000!&serverTimezone=" + TimeZone.getDefault().getID();

    public static String JDBC_Driver = null;
    public static String JDBC_URL = null;
    static Connection connection;
    private Statement statement;

    public DBManager()  throws SQLException{
        MySQLConnection();
    }

    public void MySQLConnection() throws SQLException {
        DBManager.setConnection(
                DBManager.JDBC_Driver_MySQL,
                DBManager.JDBC_URL_MySQL);
        statement = DBManager.getConnection().createStatement(
                ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
    }
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
            System.out.println("DB CONNESSO");
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

    public ResultSet selectOcchialiVistaUomo(){
            try {
                ResultSet resultSet = statement.executeQuery("SELECT o.descrizione, o.prezzo, o.percorso_immagine FROM occhiale o where o.sesso = 'M' AND o" +
                        ".tipo_occhiale = (select categoria_id from categoria c where c.nome='occhiali_vista' ) ");
                return resultSet;
            }
            catch (SQLException e)
            {
                System.out.println("ERRORE IN selectOcchialiVistaUomo() : "+e.getMessage());
                return null;
            }
    }

    public ResultSet selectOcchialiSoleUomo(){
        try {
            ResultSet resultSet = statement.executeQuery("SELECT o.descrizione, o.prezzo, o.percorso_immagine FROM occhiale o where o.sesso = 'M' AND o" +
                    ".tipo_occhiale = (select categoria_id from categoria c where c.nome='occhiali_sole' ) ");
            return resultSet;
        }
        catch (SQLException e)
        {
            System.out.println("ERRORE IN selectOcchialiSoleUomo() : "+e.getMessage());
            return null;
        }
    }
    public ResultSet selectOcchialiVistaDonna(){
        try {
            ResultSet resultSet = statement.executeQuery("SELECT o.descrizione, o.prezzo, o.percorso_immagine  FROM occhiale o where o.sesso = 'F' AND o" +
                    ".tipo_occhiale = (select categoria_id from categoria c where c.nome='occhiali_vista' ) ");
            return resultSet;
        }
        catch (SQLException e)
        {
            System.out.println("ERRORE IN selectOcchialiVistaDonna() : "+e.getMessage());
            return null;
        }
    }

    public ResultSet selectOcchialiSoleDonna(){
        try {
            ResultSet resultSet = statement.executeQuery("SELECT o.descrizione, o.prezzo, o.percorso_immagine  FROM occhiale o where o.sesso = 'F' AND o" +
                    ".tipo_occhiale = (select categoria_id from categoria c where c.nome='occhiali_sole' ) ");
            return resultSet;
        }
        catch (SQLException e)
        {
            System.out.println("ERRORE IN selectOcchialiSoleDonna() : "+e.getMessage());
            return null;
        }
    }

    public ResultSet selectLentiOcchiali(){
        try {
            ResultSet resultSet = statement.executeQuery("SELECT o.descrizione, o.prezzo, o.percorso_immagine  FROM occhiale o where o" +
                    ".tipo_occhiale = (select categoria_id from categoria c where c.nome='lenti_da_sole' ) ");
            return resultSet;
        }
        catch (SQLException e)
        {
            System.out.println("ERRORE IN selectLentiOcchiali() : "+e.getMessage());
            return null;
        }
    }

    public ResultSet selectLentiVisive(){
        try {
            ResultSet resultSet = statement.executeQuery("SELECT o.descrizione, o.prezzo, o.percorso_immagine  FROM occhiale o where o.tipo_occhiale = (select categoria_id from categoria c where c.nome='lenti' )");
            return resultSet;
        }
        catch (SQLException e)
        {
            System.out.println("ERRORE IN selectLentiVisive() : "+e.getMessage());
            return null;
        }
    }
}