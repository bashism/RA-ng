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

public class ListRelationQueryTest {
    private DB database;
    private RA ra;
    private ListRelationQuery listQuery;

    @Before
    public void setupDatabase() throws IOException, SQLException{
        Properties properties = new Properties();
                properties.load(this.getClass().getResourceAsStream("/ra.properties"));
        this.database = new DB(properties.getProperty("url"), properties);
        this.ra = new RA(new RAConfig(), database);
        this.listQuery = new ListRelationQuery(database);
    }

    @Test
    public void testQuery(){
        String query = "\\list;\n";
        String expected = ""
                + "-----\n"
                + "Bar\n"
                + "Beer\n"
                + "Drinker\n"
                + "Frequents\n"
                + "Likes\n"
                + "Serves\n"
                + "-----\n"
                + "Total of 6 table(s) found.\n\n"
                ;
        AST parsedQuery = ra.parseQuery(query);
        IQueryResult result = listQuery.query(parsedQuery);
        assertEquals(expected, result.toRawString());
    }
}
