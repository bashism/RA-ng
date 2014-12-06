package edu.duke.ra.core;

import antlr.CommonAST;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;

import java.sql.*;
import java.io.*;

public class RA {
    private RAConfig config;
    private DB database;

    public RA(RAConfig config, DB database){
        this.config = config;
        this.database = database;
    }
    public IQueryResult query(String query) throws SQLException{
        AST queryAST = parseQuery(query);
        RAXNode queryCommandTree = generateCommandTree(queryAST);
        String querySQL = generateSQLQuery(queryCommandTree, this.database);

        IQueryResult queryResult = executeQuery(querySQL);        
        return queryResult;
    }
    AST parseQuery(String query) {
        Reader queryReader = new StringReader(query);
        RALexer lexer = new RALexer(queryReader);
        RAParser parser = new RAParser(lexer);
        try {
            parser.start();
        }
        //FIXME: throw this up
        catch (RecognitionException | TokenStreamException e) {
            System.out.println("FIXME: parser has an error recognizing the query");
        }
        AST ast = parser.getAST();
        return ast;
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
        //withStatementBuilder.append("WITH ");
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

    IQueryResult executeQuery(String query) throws SQLException{
        return database.executeQuery(query);
    }
}
