package edu.duke.ra.core.query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import antlr.collections.AST;
import edu.duke.ra.core.RAException;
import edu.duke.ra.core.db.DB;
import edu.duke.ra.core.result.ErrorResult;
import edu.duke.ra.core.result.IQueryResult;
import edu.duke.ra.core.result.RawStringQueryResult;

public class SqlExecQuery extends DatabaseQuery {

    public SqlExecQuery(DB database) {
        super(database);
    }

    @Override
    public IQueryResult query(AST queryAST, String raQueryString, boolean verbose) {
        String sqlCommands = queryAST.getFirstChild().getText();
        List<RAException> errors = new ArrayList<>();
        StringBuilder out = new StringBuilder();
        try {
            database.execCommands(out, sqlCommands);
        }
        catch (SQLException exception) {
            errors.add(new RAException(
                    "SQLException",
                    "An error occurred executing custom SQL",
                    "SQL: " + raQueryString,
                    exception));
            return new ErrorResult(raQueryString, errors);
        }
        IQueryResult result = new RawStringQueryResult(raQueryString, out.toString(), errors);
        return result;
    }
}
