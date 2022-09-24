import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import com.mysql.cj.MysqlConnection;
import org.json.JSONArray;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Class for managing the REST server based on connection to a database
 */

public class RestServerResource extends ServerResource
{
    public static DBManager dbManager;

    /**
     * Universal method to get a json array of json objects taken from a query result
     * @param resultSet resultSet of the processed query
     * @return JSON array in string form
     * @throws SQLException Any SQL errors must be handled by the caller
     */
    private String resultset_to_json(ResultSet resultSet) throws SQLException
    {
        JSONArray jarr = new JSONArray();
        while (resultSet.next())
        {
            HashMap<String, String> row = new HashMap<>();
            for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++)
                row.put(resultSet.getMetaData().getColumnName(i), String.valueOf(resultSet.getObject(i)));
            jarr.put(row);
        }
        return jarr.toString();
    }

    /**
     * Method for managing incoming connections
     * @return Response to the user
     */
    @Get
    public String handleConnection()
    {
        String response = null;
        try
        {
            String request = getReference().getLastSegment();

           switch (request)
            {
                case "lentiVis":
                    response = resultset_to_json(dbManager.selectLentiVisive());
                    break;
                case "lentiOcc":
                    response = resultset_to_json(dbManager.selectLentiOcchiali());
                    break;
                case "occSoleDonna":
                    response = resultset_to_json(dbManager.selectOcchialiSoleDonna());
                    break;
                case "occVistaDonna":
                    response = resultset_to_json(dbManager.selectOcchialiVistaDonna());
                    break;
                case "occSoleUomo":
                    response = resultset_to_json(dbManager.selectOcchialiSoleUomo());
                    break;
                case "occVistaUomo":
                    response = resultset_to_json(dbManager.selectOcchialiVistaUomo());
                    break;
                case "checkUser":
                    response = String.valueOf(dbManager.checkUser(getQuery().getValues("username"), getQuery().getValues("password")));
                    break;
                case "addUser":
                    response = String.valueOf(dbManager.addUser(getQuery().getValues("name"), getQuery().getValues("surname"), getQuery().getValues("email"), getQuery().getValues("username"), getQuery().getValues("password")));
                    break;
                default:
                    throw new ResourceException(new Status(Status.SERVER_ERROR_NOT_IMPLEMENTED, ""));
            }
        } catch (Exception e)
        {
            System.out.println("[ERROR]" + getReference());
        }
        return response;
    }

    public static void main(String[] args)
    {
        try{
            dbManager = new DBManager();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }

        try
        {
            new Server(Protocol.HTTP, 4444, RestServerResource.class).start();
        } catch (Exception e)
        {
            System.out.println("Error: unable to start server");
            System.exit(1);
        }
    }
}
