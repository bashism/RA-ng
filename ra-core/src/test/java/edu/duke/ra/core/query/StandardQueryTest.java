package edu.duke.ra.core.query;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import edu.duke.ra.core.RA;
import edu.duke.ra.core.RAConfig;
import edu.duke.ra.core.RAConfigException;
import edu.duke.ra.core.ValidateException;
import edu.duke.ra.core.db.DB;
import edu.duke.ra.core.operator.RAXNode;
import edu.duke.ra.core.result.IQueryResult;

public class StandardQueryTest {
    private DB database;
    private RA ra;
    private StandardQuery standardQuery;

    @Before
    public void setupDatabase() throws IOException, SQLException, RAConfigException{
        Properties properties = new Properties();
                properties.load(this.getClass().getResourceAsStream("/ra.properties"));
        this.database = new DB(properties.getProperty("url"), properties);
        this.standardQuery = new StandardQuery(database);
        this.ra = new RA(new RAConfig.Builder().build());
    }
    /**
     * Run a simple, nested query:
     * \\project_{name} (\\select_{name = 'Corona'} beer)
     * 
     * After generating the AST, generate the command tree, which will look like:
     * RAXNode.PROJECT
     * +--RAXNode.SELECT
     *    +--RAXNode.TABLE
     * @throws TokenStreamException 
     * @throws RecognitionException 
     */
    @Test
    public void testGenerateCommandTree() throws SQLException, ValidateException, RecognitionException, TokenStreamException{
        String query = "\\project_{name} \\select_{name = 'Corona'} beer;\n";
        AST parseQueryResult = ra.parseQuery(query);

        RAXNode root = standardQuery.generateCommandTree(parseQueryResult);
        assertEquals("Project", root.getClass().getSimpleName());
        assertEquals(1, root.getNumChildren());

        RAXNode selectNode = root.getChild(0);
        assertEquals("Select", selectNode.getClass().getSimpleName());
        assertEquals(1, selectNode.getNumChildren());

        RAXNode tableNode = selectNode.getChild(0);
        assertEquals("Table",tableNode.getClass().getSimpleName());
        assertEquals(0, tableNode.getNumChildren());
    }
    /**
    * Run a simple, nested query:
    * \\project_{name} (\\select_{name = 'Corona'} beer)
    * 
    * RA will generate temporary "view" names using WITH clauses;
    * these will be appended to the beginning of the eventual SQL query;
    * the actual query itself will be those view definitions plus a select *
    * from the "view" definition of the root
     * @throws RecognitionException 
     * @throws TokenStreamException 
     * @throws ValidateException 
     * @throws SQLException 
    */
    @Test
    public void testGenerateSQLQuery() throws RecognitionException, TokenStreamException, SQLException, ValidateException{
        String query = "\\project_{name} \\select_{name = 'Corona'} beer;\n";
        AST parseQueryResult = ra.parseQuery(query);
        RAXNode root = standardQuery.generateCommandTree(parseQueryResult);
        String SqlQuery = standardQuery.generateSQLQuery(root, database).toString();
        String queryExpected = "WITH RA_TMP_VIEW_1 AS (SELECT DISTINCT * FROM beer),\n"
                        + "RA_TMP_VIEW_2 AS (SELECT * FROM RA_TMP_VIEW_1 WHERE name = 'Corona'),\n"
                        + "RA_TMP_VIEW_3 AS (SELECT DISTINCT name FROM RA_TMP_VIEW_2)\n"
                        + "SELECT * FROM RA_TMP_VIEW_3;";
        assertEquals(queryExpected, SqlQuery);
    }
    /**
     * Test actual execution of a generated query.
     * 
     * @throws SQLException If something went wrong with SQL execution
     */
    //FINISHME
    @Test
    public void testExecuteQuery() throws SQLException{
        String sqlQuery = "WITH RA_TMP_VIEW_1 AS (SELECT DISTINCT * FROM beer),\n"
                + "RA_TMP_VIEW_2 AS (SELECT * FROM RA_TMP_VIEW_1)\n"
                + "SELECT * FROM RA_TMP_VIEW_2;";
        String expected = ""
                + "Output schema: (name VARCHAR, brewer VARCHAR)\n" 
                + "-----\n" 
                + "Amstel|Amstel Brewery\n" 
                + "Budweiser|Anheuser-Busch Inc.\n" 
                + "Corona|Grupo Modelo\n" 
                + "Dixie|Dixie Brewing\n" 
                + "Erdinger|Erdinger Weissbrau\n" 
                + "Full Sail|Full Sail Brewing\n" 
                + "-----\n" 
                + "Total number of rows: 6\n\n"
                ;
        IQueryResult result = standardQuery.executeQuery(sqlQuery, "asdf", false);
        assertEquals("edu.duke.ra.core.result.StandardQueryResult", result.getClass().getName());
    }
}
