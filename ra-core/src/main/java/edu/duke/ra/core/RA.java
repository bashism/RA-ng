package edu.duke.ra.core;

import antlr.CommonAST;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;

import java.sql.*;
import java.io.*;

import edu.duke.ra.core.db.DB;
import edu.duke.ra.core.operator.RAXNode;
import edu.duke.ra.core.query.ListQuery;
import edu.duke.ra.core.query.SqlExecQuery;
import edu.duke.ra.core.query.StandardQuery;
import edu.duke.ra.core.result.HelpQueryResult;
import edu.duke.ra.core.result.IQueryResult;
import edu.duke.ra.core.result.QuitQueryResult;

public class RA {
    private RAConfig config;
    private DB database;

    public RA(RAConfig config, DB database){
        this.config = config;
        this.database = database;
    }
    public IQueryResult query(String query) throws SQLException{
        AST queryAST = parseQuery(query);
        switch (queryAST.getType()) {
            case RALexerTokenTypes.QUIT: case RALexerTokenTypes.EOF:
                return new QuitQueryResult();
            case RALexerTokenTypes.HELP:
                return new HelpQueryResult();
            case RALexerTokenTypes.LIST:
                return new ListQuery(database).query(queryAST);
            case RALexerTokenTypes.SQLEXEC:
                return new SqlExecQuery(database).query(queryAST);
            default:
                return new StandardQuery(database).query(queryAST);
        }
    }
    public AST parseQuery(String query) {
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
}
