package edu.duke.ra.core.query;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import antlr.collections.AST;
import edu.duke.ra.core.RA;
import edu.duke.ra.core.RAConfig;
import edu.duke.ra.core.RAConfigException;
import edu.duke.ra.core.db.DB;
import edu.duke.ra.core.result.IQueryResult;

public class ListRelationQueryTest {
    private RA ra;

    @Before
    public void setupDatabase() throws IOException, SQLException, RAConfigException{
        this.ra = new RA(new RAConfig.Builder().build());
    }

    @Test
    public void testQuery(){
        String query = "\\list;\n";
        String expectedEntries =
            "[\"Bar\",\"Beer\",\"Drinker\",\"Frequents\",\"Likes\",\"Serves\"]";
        IQueryResult result = ra.query(query);
        assertEquals("edu.duke.ra.core.result.ListRelationQueryResult",result.getClass().getName());
        JSONObject resultJson = new JSONObject(result.toJsonString());
        assertEquals(query, resultJson.getString("query"));
        assertEquals(expectedEntries, resultJson.getJSONObject("data").getJSONArray("relations").toString());
    }
}
