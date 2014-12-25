package edu.duke.ra.core.operator;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.PrintStream;
import java.sql.SQLException;

import edu.duke.ra.core.ValidateException;
import edu.duke.ra.core.db.DB;
import edu.duke.ra.core.db.TableSchema;

public abstract class RAXNode {

    protected static int _viewGeneratedCount = 0;
    public static String generateViewName() {
        _viewGeneratedCount++;
        return "RA_TMP_VIEW_" + _viewGeneratedCount;
    }
    public static void resetViewNameGenerator() {
        _viewGeneratedCount = 0;
    }

    public enum Status { ERROR, UNCHECKED, CORRECT }

    protected Status _status;
    protected String _viewName;
    protected TableSchema _outputSchema;
    protected ArrayList<RAXNode> _children;
    protected RAXNode(ArrayList<RAXNode> children) {
        _status = Status.UNCHECKED;
        _viewName = generateViewName();
        _outputSchema = null;
        _children = children;
    }
    public String getViewName() {
        return _viewName;
    }
    public int getNumChildren() {
        return _children.size();
    }
    public RAXNode getChild(int i) {
        return _children.get(i);
    }
    public abstract String genViewDef(DB db)
        throws SQLException, ValidateException;
    public String genViewCreateStatement(DB db)
        throws SQLException, ValidateException {
        return "CREATE VIEW " + _viewName + " AS " + genViewDef(db);
    }
    public abstract String toPrintString();
    public void print(boolean verbose, int indent, PrintStream out) {
        for (int i=0; i<indent; i++) out.print(" ");
        out.print(toPrintString());
        if (verbose) {
            if (_status == Status.CORRECT) {
                out.print(" <- output schema: " + _outputSchema.toPrintString());
            } else if (_status == Status.ERROR) {
                out.print(" <- ERROR!");
            }
        }
        out.println();
        for (int i=0; i<getNumChildren(); i++) {
            getChild(i).print(verbose, indent+4, out);
        }
        return;
    }

    public static List<String> parseColumnNames(String columns) {
        String[] columnNames = columns.split("\\s*,\\s*");
        return Arrays.asList(columnNames);
    }
}
