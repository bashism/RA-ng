package edu.duke.ra.core.query;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

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

    //FINISHME
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
        IQueryResult result = ra.query(query);
        assertEquals("edu.duke.ra.core.result.ListRelationQueryResult",result.getClass().getName());
    }
}
