package edu.duke.ra.core.query;

import java.sql.SQLException;

import antlr.collections.AST;
import edu.duke.ra.core.db.DB;
import edu.duke.ra.core.result.IQueryResult;
import edu.duke.ra.core.result.RawStringQueryResult;

public class SqlExecQuery extends DatabaseQuery {

    public SqlExecQuery(DB database) {
        super(database);
    }

    @Override
    public IQueryResult query(AST queryAST) throws SQLException {
        String sqlCommands = queryAST.getFirstChild().getText();
        StringBuilder out = new StringBuilder();
        database.execCommands(out, sqlCommands);
        IQueryResult result = new RawStringQueryResult(out.toString());
        return result;
    }
}
