package edu.duke.ra.core.query;

import java.sql.SQLException;

import antlr.RecognitionException;
import antlr.collections.AST;
import edu.duke.ra.core.ValidateException;
import edu.duke.ra.core.db.DB;
import edu.duke.ra.core.operator.RAXNode;
import edu.duke.ra.core.result.IQueryResult;
import edu.duke.ra.core.RAXConstructor;

public class StandardQuery extends DatabaseQuery {

    public StandardQuery(DB database) {
        super(database);
    }

    @Override
    public IQueryResult query(AST queryAST) throws SQLException {
        RAXNode queryCommandTree = generateCommandTree(queryAST);
        String querySQL = generateSQLQuery(queryCommandTree, this.database);
        IQueryResult queryResult = executeQuery(querySQL);        
        return queryResult;
    }

    RAXNode generateCommandTree(AST ast){
        RAXConstructor constructor = new RAXConstructor();
        RAXNode raxTree = null;
        try {
            raxTree = constructor.expr(ast);
        }
        //FIXME: throw this up
        catch (RecognitionException e) {
            System.out.println("FIXME: parser error");
        }
        return raxTree;
        
    }

    String generateSQLQuery(RAXNode commandRoot, DB database) {
        StringBuilder querySQL = new StringBuilder("WITH ");
        querySQL.append(generateTempViewDefinitions(commandRoot, database, 0));
        querySQL.append("SELECT * FROM " + commandRoot.getViewName() + ";");
        return querySQL.toString();
    }

    StringBuilder generateTempViewDefinitions(RAXNode commandRoot, DB database, int depth){
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
    StringBuilder generateWithStatement(RAXNode node, DB database) {
        StringBuilder withStatementBuilder = new StringBuilder();
        withStatementBuilder.append(node.getViewName());
        withStatementBuilder.append(" AS (");
        try {
            withStatementBuilder.append(node.genViewDef(database));
        }
        //FIXME
        catch (SQLException | ValidateException e) {
            System.out.println("FIXME: SQL error");
        }
        withStatementBuilder.append(")");
        return withStatementBuilder;
    }

    IQueryResult executeQuery(String sqlQuery) throws SQLException{
        return database.executeQuery(sqlQuery);
    }
}
