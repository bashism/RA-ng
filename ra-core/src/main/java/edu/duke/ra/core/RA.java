package edu.duke.ra.core;

import antlr.CommonAST;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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

    public IQueryResult query(String query) {
        ValueWithError<AST> queryASTGenerationResult = makeQueryAST(query);
        if (queryASTGenerationResult.hasError()) {
            System.out.println(queryASTGenerationResult.error());
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
