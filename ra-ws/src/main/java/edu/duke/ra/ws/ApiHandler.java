package edu.duke.ra.ws;

import java.io.IOException;
import java.sql.SQLException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import edu.duke.ra.core.RA;
import edu.duke.ra.core.RAConfig;
import edu.duke.ra.core.RAConfigException;

/**
 * The request handler for the RA web API
 */
@Path("api/query")
public class ApiHandler {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String query(@QueryParam("query") String queryString) {
        try {
            RAConfig config = new RAConfig.Builder().url("jdbc:sqlite://sample.db").build();
            RA ra = new RA(config);
            String fullQueryString = queryString + ";\n";
            String result = ra.query(fullQueryString).toJsonString();
            ra.closeDBConnection();
            return result;
        } catch (IOException | SQLException | RAConfigException e) {
            return e.getMessage();
        }
    }
}
