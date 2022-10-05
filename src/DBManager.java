import org.json.JSONArray;

import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.util.Date;
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

    public String Utente="";
    public static final String JDBC_Driver_MySQL = "com.mysql.cj.jdbc.Driver";
    public static final String JDBC_URL_MySQL = "jdbc:mysql://localhost:3306/myglassdb?user=marco&password" +
            "=ciccio2000!&serverTimezone=" + TimeZone.getDefault().getID();

    public static String JDBC_Driver = null;
    public static String JDBC_URL = null;
    static Connection connection;
    private Statement statement;
    ResultSet resultSet;
    public DBManager()  throws SQLException{
        try{
            MySQLConnection();
            System.out.println("Connection DB made");
        }catch (Exception e){
            System.out.println("Connection DB fail");
            System.out.println(e.getMessage());
        }

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
    public boolean deleteItem(String occhiale_id) {
            try {
                int res =
                        statement.executeUpdate("UPDATE occhiale_ordinato SET ordinato = 1 WHERE occhiale_id = " + occhiale_id);
                return true;
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return false;
            }
    }

    public boolean addShop(String occhiale_id){

        try {
            String carrello_id = "";
            //username dell'utente
            String utente_fk= Utente;

            //prendo la data di oggi
            LocalDate date = LocalDate. now();
            String data = date.toString();

            //controllo se esiste già il carrello associato all'utente
            resultSet = statement.executeQuery("select carrello_id as id, utente_fk as fk, data_creazione as data " +
                    "from carrello where utente_fk='"+utente_fk+"'");

            if(resultSet.next()){
                    carrello_id = resultSet.getString("id");
                    int res = statement.executeUpdate("INSERT INTO occhiale_ordinato (occhiale_id,utente_id," +
                            "carrello_id,ordinato) VALUES ("+occhiale_id+",'"+utente_fk+"',"+carrello_id+",0)");

            }
            else{
                //vado a creare il carrello dell'utente
                resultSet = statement.executeQuery("select max(carrello_id) + 1 as id from carrello WHERE " +
                                "utente_fk='" +utente_fk+"'");
                resultSet.next();
                carrello_id = resultSet.getString("id");
                if(carrello_id == null)carrello_id="1";
                int res =
                        statement.executeUpdate("INSERT INTO carrello (carrello_id,utente_fk,data_creazione) " +
                                "VALUES " +
                                "("+ carrello_id.toString() +",'"+utente_fk.toString()+"','"+data+"')");


                //vado ad inserire il prodotto
                resultSet = statement.executeQuery("select carrello_id as id, utente_fk as fk, data_creazione as data " +
                        "from carrello where utente_fk='"+utente_fk+"'");
                resultSet.next();
                carrello_id = resultSet.getString("id");

                 res = statement.executeUpdate("INSERT INTO occhiale_ordinato (occhiale_id,utente_id," +
                        "carrello_id,ordinato) VALUES ("+occhiale_id+",'"+utente_fk+"',"+carrello_id+",0)");
            }

            return true;
        }catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }

    }

    public boolean checkUser(String username, String password) {
        if (password != null) {
            try {
                resultSet = statement.executeQuery("select u.username, u.password from utente u where username='" + username + "' and password ='" + password + "'");
                resultSet.next();
                Utente =  resultSet.getString("username");
                return true;
            } catch (SQLException e) {
                return false;
            }
        }

        try {
            resultSet = statement.executeQuery("select u.username from utente u where username='" + username + "'");
            resultSet.next();
            Utente =  resultSet.getString("username");
            return false;
        } catch (SQLException e) {
            return true;
        }
    }

    public boolean addUser(String username, String password, String nome, String cognome, String email) {
        if ((username != null) & (password != null) & (nome != null) & (cognome != null)) {
            try {
                int res =
                        statement.executeUpdate("INSERT INTO utente (username,password,nome,cognome,email) VALUES " +
                                "('"+username+"','"+password+"','"+nome+"','"+cognome+"','"+email+"')");
                return true;
            } catch (SQLException e) {
                return false;
            }
        }
        return false;
    }

    public ResultSet loadShop() {

        resultSet = null;
        try {
            //sleziono tutti gli occhiali aggiunti al carrello ma non ancora ordinati
            resultSet = statement.executeQuery("SELECT o.*, c.nome " + "FROM occhiale o JOIN occhiale_ordinato oo ON (oo" +
                    ".occhiale_id = o.occhiale_id) JOIN categoria c ON (o.tipo_occhiale = c.categoria_id)" + "WHERE oo.ordinato = 0; ");

            return resultSet;
        } catch (SQLException e) {
            System.out.println("Error");
        }
        return resultSet;
    }

    /**
     * Method that selects the description, price and image path of the glasses
     *
     * @return a resulSet of all data read from the db
     */
    public ResultSet getallCatalogue() {

        resultSet = null;
        try {
            resultSet = statement.executeQuery("select o.occhiale_id, o.descrizione, o.prezzo, o.percorso_immagine, c" +
                    ".nome " +
                    "from occhiale o join categoria c on (c.categoria_id = o.tipo_occhiale)");
        } catch (SQLException e) {
            System.out.println("Error");
        }
        return resultSet;
    }

    public ResultSet selectOcchialiVistaUomo() {

        resultSet = null;
        try {
            resultSet = statement.executeQuery("select o.occhiale_id, o.descrizione, o.prezzo, o.percorso_immagine, c" +
                    ".nome from occhiale o join categoria c on (c.categoria_id = o.tipo_occhiale) where o.sesso = 'M' AND c.nome='occhiali_vista'");
            return resultSet;
        } catch (SQLException e) {
            System.out.println("Error");
        }
        return resultSet;
    }

    public ResultSet selectOcchialiSoleUomo() {

        resultSet = null;
        try {
            resultSet = statement.executeQuery("select o.occhiale_id, o.descrizione, o.prezzo, o.percorso_immagine, c.nome from " +
                    "occhiale o join categoria c on (c.categoria_id = o.tipo_occhiale) where o.sesso = 'M' AND c.nome='occhiali_sole'");
            return resultSet;
        } catch (SQLException e) {
            System.out.println("Error");
        }
        return resultSet;
    }

    public ResultSet selectOcchialiVistaDonna() {

        resultSet = null;
        try {
            resultSet = statement.executeQuery("select o.occhiale_id, o.descrizione, o.prezzo, o.percorso_immagine, c.nome from " +
                    "occhiale o join categoria c on (c.categoria_id = o.tipo_occhiale) where o.sesso = 'F' AND c.nome='occhiali_vista'");
            return resultSet;
        } catch (SQLException e) {
            System.out.println("Error");
        }
        return resultSet;
    }

    public ResultSet selectOcchialiSoleDonna() {

        resultSet = null;
        try {
            ResultSet resultSet = statement.executeQuery("select o.occhiale_id, o.descrizione, o.prezzo, o.percorso_immagine, c" +
                    ".nome from occhiale o join categoria c on (c.categoria_id = o.tipo_occhiale) where o.sesso = 'F' AND c.nome='occhiali_sole'");
            return resultSet;
        } catch (SQLException e) {
            System.out.println("Error");
        }
        return resultSet;

    }

    public ResultSet selectLentiSole() {

        resultSet = null;
        try {
            ResultSet resultSet = statement.executeQuery("select o.occhiale_id, o.descrizione, o.prezzo, o.percorso_immagine, c" +
                    ".nome from occhiale o join categoria c on (c.categoria_id = o.tipo_occhiale) where c.nome='lenti_da_sole'");
            return resultSet;
        } catch (SQLException e) {
            System.out.println("Error");
        }
        return resultSet;

    }

    public ResultSet selectLentiVista() {

        resultSet = null;
        try {
            ResultSet resultSet = statement.executeQuery("SELECT o.occhiale_id, o.descrizione, o.prezzo, o" +
                    ".percorso_immagine  FROM occhiale o where o.tipo_occhiale = (select categoria_id from categoria c where c.nome='lenti' )");
            return resultSet;
        } catch (SQLException e) {
            System.out.println("Error");
        }
        return resultSet;

    }
}