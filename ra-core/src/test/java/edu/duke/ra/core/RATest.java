package edu.duke.ra.core;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.Test;
import org.junit.Before;
import org.junit.BeforeClass;

import antlr.CommonAST;
import antlr.collections.AST;

public class RATest {
    private DB database;
    private RA ra;

    @Before
    public void setupDatabase() throws IOException, SQLException{
        Properties properties = new Properties();
		properties.load(this.getClass().getResourceAsStream("/ra.properties"));
        this.database = new DB(properties.getProperty("url"), properties);
        this.ra = new RA(new RAConfig(), database);
    }
    /**
     * Run a simple, nested query:
     * \\project_{name} (\\select_{name = 'Corona'} beer)
     * 
     *  Should generate this AST:
     *  \project
     *  +--name
     *  +--\select
     *     +--name = 'Corona'
     *     +--beer
     */
    @Test
    public void testParseQuery(){
        //product placement
        String query = "\\project_{name} \\select_{name = 'Corona'} beer;\n";

        AST parseQueryResult = ra.parseQuery(query);
        assertEquals("\\project", parseQueryResult.getText());
        assertEquals(2, parseQueryResult.getNumberOfChildren());

        AST projectPredicate = parseQueryResult.getFirstChild();
        assertEquals("name", projectPredicate.getText());
        assertEquals(0, projectPredicate.getNumberOfChildren());

        AST selectOperator = parseQueryResult.getFirstChild().getNextSibling();
        assertEquals("\\select", selectOperator.getText());
        assertEquals(2, selectOperator.getNumberOfChildren());

        AST selectPredicate = selectOperator.getFirstChild();
        assertEquals("name = 'Corona'", selectPredicate.getText());
        assertEquals(0, selectPredicate.getNumberOfChildren());

        AST selectRelation = selectPredicate.getNextSibling();
        assertEquals("beer", selectRelation.getText());
        assertEquals(0, selectRelation.getNumberOfChildren());
    }
    /**
     * Run a simple, nested query:
     * \\project_{name} (\\select_{name = 'Corona'} beer)
     * 
     * After generating the AST, generate the command tree, which will look like:
     * RAXNode.PROJECT
     * +--RAXNode.SELECT
     *    +--RAXNode.TABLE
     */
    @Test
    public void testGenerateCommandTree() throws SQLException, ValidateException{
        String query = "\\project_{name} \\select_{name = 'Corona'} beer;\n";
        AST parseQueryResult = ra.parseQuery(query);

        RAXNode root = ra.generateCommandTree(parseQueryResult);
        assertEquals("PROJECT", root.getClass().getSimpleName());
        assertEquals(1, root.getNumChildren());

        RAXNode selectNode = root.getChild(0);
        assertEquals("SELECT", selectNode.getClass().getSimpleName());
        assertEquals(1, selectNode.getNumChildren());

        RAXNode tableNode = selectNode.getChild(0);
        assertEquals("TABLE",tableNode.getClass().getSimpleName());
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
    */
    @Test
    public void testGenerateSQLQuery(){
        String query = "\\project_{name} \\select_{name = 'Corona'} beer;\n";
        AST parseQueryResult = ra.parseQuery(query);
        RAXNode root = ra.generateCommandTree(parseQueryResult);
        String SqlQuery = ra.generateSQLQuery(root, database);
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
    @Test
    public void testExecuteQuery() throws SQLException{
        String sqlQuery = "WITH RA_TMP_VIEW_1 AS (SELECT DISTINCT * FROM beer),\n"
                + "RA_TMP_VIEW_2 AS (SELECT * FROM RA_TMP_VIEW_1)\n"
                + "SELECT * FROM RA_TMP_VIEW_2;";
        QueryResult result = ra.executeQuery(sqlQuery);
    }
}
