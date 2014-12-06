package edu.duke.ra.core;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.Test;
import org.junit.Before;
import org.junit.BeforeClass;

import edu.duke.ra.core.db.DB;
import edu.duke.ra.core.operator.RAXNode;
import edu.duke.ra.core.result.IQueryResult;
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
}
