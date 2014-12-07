package edu.duke.ra.core.query;

import java.sql.SQLException;
import java.util.ArrayList;

import antlr.collections.AST;
import edu.duke.ra.core.db.DB;
import edu.duke.ra.core.result.IQueryResult;
import edu.duke.ra.core.result.ListRelationQueryResult;

public class ListRelationQuery extends DatabaseQuery{

    public ListRelationQuery(DB database) {
        super(database);
    }

    @Override
    public IQueryResult query(AST queryAST) {
        ArrayList<String> tables = null;
        try {
            tables = database.getTables();
        } catch (SQLException e) {
            System.out.println("Unexpected error obtaining list of tables from database");
            // FIXME: implement verbosity with queries
            /*
            err.println("Unexpected error obtaining list of tables from database");
            database.printSQLExceptionDetails(e, err, verbose);
            err.println();
            */
        }
        IQueryResult result = new ListRelationQueryResult(tables);
        return result;
    }

}
