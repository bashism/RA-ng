package edu.duke.ra.core.query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import antlr.RecognitionException;
import antlr.collections.AST;
import edu.duke.ra.core.RAException;
import edu.duke.ra.core.ValidateException;
import edu.duke.ra.core.db.DB;
import edu.duke.ra.core.operator.RAXNode;
import edu.duke.ra.core.result.ErrorResult;
import edu.duke.ra.core.result.IQueryResult;
// this should be left uncommented, but if you want to get full IDE syntax
// validation you may have to comment this out temporarily
import edu.duke.ra.core.RAXConstructor;
import edu.duke.ra.core.result.ValueWithError;

public class StandardQuery extends DatabaseQuery {

    public StandardQuery(DB database) {
        super(database);
    }

    @Override
    public IQueryResult query(AST queryAST, String raQueryString, boolean verbose) {
        ValueWithError<RAXNode> commandGenerationResult =
                generateCommandTree(queryAST, raQueryString, verbose);
        if (commandGenerationResult.hasError()) {
            return commandGenerationResult.error();
        }
        RAXNode queryCommandTree = commandGenerationResult.value();

        ValueWithError<String> generateSQLResult = generateSQLQuery(raQueryString, queryCommandTree, verbose);
        if (generateSQLResult.hasError()) {
            return generateSQLResult.error();
        }
        String querySQL = generateSQLResult.value();
        IQueryResult queryResult = executeQuery(querySQL, raQueryString, verbose);        
        return queryResult;
    }

    ValueWithError<RAXNode> generateCommandTree(AST queryAST, String raQueryString, boolean verbose){
        List<RAException> errors = new ArrayList<>();
        try {
            RAXNode queryCommandTree = generateCommandTree(queryAST);
            queryCommandTree.validate(database);
            return new ValueWithError<RAXNode>(queryCommandTree, null);
        }
        catch (RecognitionException exception) {
            errors.add(new RAException(
                    "RecognitionException",
                    "An syntax error generating commands",
                    "Line: " + exception.getLine() + "\n"
                    + "Column: " + exception.getColumn() + "\n",
                    exception));
            return new ValueWithError<RAXNode>(null, new ErrorResult(raQueryString, errors));
        }
        catch (ValidateException exception) {
            errors.add(new RAException(
                    "ValidateException",
                    "An error validating the commands",
                    "Command: " + exception.getErrorNode().toPrintString() + "\n",
                    exception));
            return new ValueWithError<RAXNode>(null, new ErrorResult(raQueryString, errors));
        }
    }

    RAXNode generateCommandTree(AST ast) throws RecognitionException {
        RAXConstructor constructor = new RAXConstructor();
        RAXNode raxTree = null;
        // throws RecognitionException
        raxTree = constructor.expr(ast);
        return raxTree;
    }

    ValueWithError<String> generateSQLQuery(String raQueryString, RAXNode queryCommandTree, boolean verbose) {
        List<RAException> errors = new ArrayList<>();
        try {
            String querySQL = generateSQLQuery(queryCommandTree, this.database);
            return new ValueWithError<String>(querySQL, null);
        }
        catch (SQLException exception) {
            errors.add(new RAException(
                    "SQLException",
                    "An error generating the SQL of this RA command",
                    "SQLState: " + exception.getSQLState() + "\n",
                    exception));
            return new ValueWithError<String>(null, new ErrorResult(raQueryString, errors));
        }
        catch (ValidateException exception) {
            errors.add(new RAException(
                    "ValidateException",
                    "An error validating the RA commands",
                    "Command: " + exception.getErrorNode().toPrintString() + "\n",
                    exception));
            return new ValueWithError<String>(null, new ErrorResult(raQueryString, errors));
        }
    }

    String generateSQLQuery(RAXNode commandRoot, DB database)
            throws SQLException, ValidateException {
        StringBuilder querySQL = new StringBuilder("WITH ");
        querySQL.append(generateTempViewDefinitions(commandRoot, database, 0));
        querySQL.append("SELECT * FROM " + commandRoot.getViewName() + ";");
        return querySQL.toString();
    }

    StringBuilder generateTempViewDefinitions(RAXNode commandRoot, DB database, int depth)
            throws SQLException, ValidateException {
        StringBuilder queryBuilder = new StringBuilder();
        for (int i = 0; i < commandRoot.getNumChildren(); i++) {
            queryBuilder.append(generateTempViewDefinitions(commandRoot.getChild(i), database, depth + 1));
        }
        queryBuilder.append(generateWithStatement(commandRoot, database));
        if (depth > 0) {
            queryBuilder.append(",");
        }
        queryBuilder.append("\n");
        return queryBuilder;
    }
    StringBuilder generateWithStatement(RAXNode node, DB database)
            throws SQLException, ValidateException{
        StringBuilder withStatementBuilder = new StringBuilder();
        withStatementBuilder.append(node.getViewName());
        withStatementBuilder.append(" AS (");
        // throws SQLException, ValidateException
        withStatementBuilder.append(node.genViewDef(database));
        withStatementBuilder.append(")");
        return withStatementBuilder;
    }

    IQueryResult executeQuery(String sqlQuery, String raQueryString, boolean verbose) {
        List<RAException> errors = new ArrayList<>();
        try {
            return database.executeQuery(sqlQuery, raQueryString, verbose);
        } catch (SQLException exception) {
            errors.add(new RAException(
                    "SQLException",
                    "An error executing the SQL query",
                    "SQLState: " + exception.getSQLState() + "\n",
                    exception));
            return new ErrorResult(raQueryString, errors);
        }
    }
}
