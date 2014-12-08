package edu.duke.ra.core;

import antlr.CommonAST;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

import edu.duke.ra.core.db.DB;
import edu.duke.ra.core.operator.RAXNode;
import edu.duke.ra.core.query.ListRelationQuery;
import edu.duke.ra.core.query.SqlExecQuery;
import edu.duke.ra.core.query.StandardQuery;
import edu.duke.ra.core.result.ErrorResult;
import edu.duke.ra.core.result.HelpQueryResult;
import edu.duke.ra.core.result.IQueryResult;
import edu.duke.ra.core.result.QuitQueryResult;
import edu.duke.ra.core.result.ValueWithError;

public class RA {
    private RAConfig config;
    private DB database;

    public RA(RAConfig config) throws IOException, SQLException{
        this.config = config;
        this.database = config.configureDB();
    }
    
    public void closeDBConnection(){
        try {
            if (database != null) database.close();
        } catch (SQLException e) {
            // Simply ignore.
        }
    }

    public IQueryResult query(String query) {
        ValueWithError<String> strippedTables = validateAndStripTableReferences(query);
        if (strippedTables.hasError()) {
            return strippedTables.error();
        }
        String queryInternal = strippedTables.value();
        ValueWithError<AST> queryASTGenerationResult = makeQueryAST(queryInternal);
        if (queryASTGenerationResult.hasError()) {
            return queryASTGenerationResult.error();
        }
        AST queryAST = queryASTGenerationResult.value();

        switch (queryAST.getType()) {
            case RALexerTokenTypes.QUIT: case RALexerTokenTypes.EOF:
                return new QuitQueryResult();
            case RALexerTokenTypes.HELP:
                return new HelpQueryResult();
            case RALexerTokenTypes.LIST:
                return new ListRelationQuery(database).query(queryAST, query, this.config.verbose());
            case RALexerTokenTypes.SQLEXEC:
                return new SqlExecQuery(database).query(queryAST, query, this.config.verbose());
            default:
                return new StandardQuery(database).query(queryAST, query, this.config.verbose());
        }
    }
    public AST parseQuery(String query) throws RecognitionException, TokenStreamException {
        Reader queryReader = new StringReader(query);
        RALexer lexer = new RALexer(queryReader);
        RAParser parser = new RAParser(lexer);
        parser.start();
        AST ast = parser.getAST();
        return ast;
    }
    ValueWithError<String> validateAndStripTableReferences(String rawQuery) {
        Pattern tableName = Pattern.compile("@(\\w+(\\.\\w+)?)");
        Matcher tableMatches = tableName.matcher(rawQuery);
        List<String> tables = new ArrayList<>();
        while (tableMatches.find()){
            String columnReference = tableMatches.group(1);
            String table = columnReference.split("\\.")[0];
            tables.add(table);
        }
        List<RAException> errors = new ArrayList<>();
        List<String> dbTables;
        try {
            dbTables = database.getTables();
        } catch (SQLException exception) {
            errors.add(new RAException(
                    "SQLException",
                    "Error retrieving tables from the database",
                    "SQLState: " + exception.getSQLState() + "\n",
                    exception));
            return new ValueWithError<String>(
                    null, new ErrorResult(rawQuery, errors));
        }
        boolean hasErrors = false;
        for (String table: tables) {
            if (! dbTables.contains(table)) {
                hasErrors = true;
                errors.add(new RAException(
                        "InvalidTableReference",
                        "The referenced table does not exist in the database",
                        "Table: " + table + "\n",
                        new Exception()));
            }
        }
        if (hasErrors) {
            return new ValueWithError<String>(
                    null, new ErrorResult(rawQuery, errors));
        }
        String query = tableMatches.replaceAll("$1");
        return new ValueWithError<String>(query, null);
    }
    ValueWithError<AST> makeQueryAST(String query) {
        List<RAException> errors = new ArrayList<>();
        try {
            AST queryAST = parseQuery(query);
            return new ValueWithError<AST>(queryAST, null);
        }
        catch (RecognitionException exception) {
            RAException newError = new RAException(
                    "RecognitionException", 
                    "An error parsing the RA query string",
                    "Line: " + exception.getLine() + "\n"
                    + "Column: " + exception.getColumn() + "\n",
                    exception);
            errors.add(newError);
            return new ValueWithError<AST>(null, new ErrorResult(query, errors));
        }
        catch (TokenStreamException exception) {
            RAException newError = new RAException(
                    "TokenStreamException",
                    "An error generating tokens for this RA query string",
                    "",
                    exception);
            errors.add(newError);
            return new ValueWithError<AST>(null, new ErrorResult(query, errors));
        }
    }
}
