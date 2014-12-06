package edu.duke.ra.core.operator;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.PrintStream;
import java.sql.SQLException;

import edu.duke.ra.core.ValidateException;
import edu.duke.ra.core.db.DB;

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
    protected DB.TableSchema _outputSchema;
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
    public void validate(DB db)
        throws ValidateException {
        // Validate children first; any exception thrown there
        // will shortcut the call.
        for (int i=0; i<getNumChildren(); i++) {
            getChild(i).validate(db);
        }
        try {
            // Drop the view, just in case it is left over from
            // a previous run (shouldn't have happened if it was
            // a clean run):
            db.dropView(_viewName);
        } catch (SQLException e) {
            // Simply ignore; this is probably not safe.  I would
            // imagine that we are trying to drop view8 as the root
            // view, but in a previous run view8 is used to define
            // view9, so view8 cannot be dropped before view9.  A
            // robust solution seems nasty.
        }
        try {
            db.createView(genViewCreateStatement(db));
            _outputSchema = db.getTableSchema(_viewName);
            assert(_outputSchema != null);
        } catch (SQLException e) {
            _status = Status.ERROR;
            // Wrap and re-throw the exception for caller to handle.
            throw new ValidateException(e, this);
        }
        // Everything rooted at this node went smoothly.
        _status = Status.CORRECT;
        return;
    }
    public void execute(DB db, PrintStream out)
        throws SQLException {
        assert(_status == Status.CORRECT);
        db.execQueryAndOutputResult(out, "SELECT * FROM " + _viewName);
        return;
    }
    public void clean(DB db) 
        throws SQLException {
        if (_status == Status.UNCHECKED) {
            // Should be the case that the view wasn't actually created.
        } else if (_status == Status.CORRECT) {
            db.dropView(_viewName);
            _status = Status.UNCHECKED;
        } else if (_status == Status.ERROR) {
            // The view shouldn't have been created successfully; no
            // need to drop.
            _status = Status.UNCHECKED;
        }
        for (int i=0; i<getNumChildren(); i++) {
            getChild(i).clean(db);
        }
        return;
    }

    public static List<String> parseColumnNames(String columns) {
        String[] columnNames = columns.split("\\s*,\\s*");
        return Arrays.asList(columnNames);
    }
}
