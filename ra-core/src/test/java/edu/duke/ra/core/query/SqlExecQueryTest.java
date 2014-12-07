package edu.duke.ra.core.query;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import antlr.collections.AST;
import edu.duke.ra.core.RA;
import edu.duke.ra.core.RAConfig;
import edu.duke.ra.core.db.DB;
import edu.duke.ra.core.result.IQueryResult;

public class SqlExecQueryTest {
    private DB database;
    private RA ra;
    private SqlExecQuery listQuery;

    @Before
    public void setupDatabase() throws IOException, SQLException{
        Properties properties = new Properties();
                properties.load(this.getClass().getResourceAsStream("/ra.properties"));
        this.database = new DB(properties.getProperty("url"), properties);
        this.ra = new RA(new RAConfig(), database);
        this.listQuery = new SqlExecQuery(database);
    }
    @Test
    public void testQuery() throws SQLException{
        String query = "\\sqlexec_{SELECT * FROM Bar;};\n";
        String expected = ""
                + "*** Result 1 is a table:\n"
                + "Output schema: (name VARCHAR, address VARCHAR)\n"
                + "-----\n"
                + "Down Under Pub|802 W. Main Street\n"
                + "The Edge|108 Morris Street\n"
                + "James Joyce Pub|912 W. Main Street\n"
                + "Satisfaction|905 W. Main Street\n"
                + "Talk of the Town|108 E. Main Street\n"
                + "-----\n"
                + "Total number of rows: 5\n\n"
                ;
        AST parsedQuery = ra.parseQuery(query);
        IQueryResult result = listQuery.query(parsedQuery);
        assertEquals(expected, result.toRawString());
    }
}
