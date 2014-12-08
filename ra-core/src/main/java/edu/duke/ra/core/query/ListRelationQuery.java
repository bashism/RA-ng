package edu.duke.ra.core.query;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import antlr.collections.AST;
import edu.duke.ra.core.RAException;
import edu.duke.ra.core.db.DB;
import edu.duke.ra.core.result.ErrorResult;
import edu.duke.ra.core.result.IQueryResult;
import edu.duke.ra.core.result.ListRelationQueryResult;

public class ListRelationQuery extends DatabaseQuery{

    public ListRelationQuery(DB database) {
        super(database);
    }

    @Override
    public IQueryResult query(AST queryAST, String raQueryString, boolean verbose) {
        List<String> relations = null;
        List<RAException> errors = new ArrayList<>();
        try {
            relations = database.getTables();
        } catch (SQLException exception) {
            ByteArrayOutputStream details = new ByteArrayOutputStream();
            PrintStream toPrint = new PrintStream(details);
            DB.printSQLExceptionDetails(exception, toPrint, verbose);
            errors.add(new RAException(
                    "SQLException",
                    "Unexpected error obtaining list of tables from database",
                    details.toString(),
                    exception));
            return new ErrorResult(raQueryString, errors);
        }
        IQueryResult result = new ListRelationQueryResult(relations, errors);
        return result;
    }

}
