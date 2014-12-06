package edu.duke.ra.core.query;

import java.sql.SQLException;
import antlr.collections.AST;
import edu.duke.ra.core.result.IQueryResult;

/**
 * An interface representing a query to execute
 *
 */
public interface IQuery {
    /**
     * Execute a query based on a generated AST
     * 
     * @param query The AST to use for this query
     * @return The result of the query
     * @throws SQLException If a SQL error occurred during execution
     */
    public IQueryResult query(AST queryAST) throws SQLException;
}
