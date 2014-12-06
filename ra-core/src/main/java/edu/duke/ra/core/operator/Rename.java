package edu.duke.ra.core.operator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.duke.ra.core.ValidateException;
import edu.duke.ra.core.db.DB;

public class Rename extends RAXNode{
    protected String _columns;
    public Rename(String columns, RAXNode input) {
        super(new ArrayList<RAXNode>(Arrays.asList(input)));
        _columns = columns;
    }
    public String genViewDef(DB db)
        throws SQLException, ValidateException {
        if (db.getDriverName().equals("org.sqlite.JDBC")) {
            // SQLite doesn't allows view column names to be
            // specified, so we have to dissect the list of new
            // column names and build the SELECT clause.
            // First, get the input schema of the child, which
            // should have already been validated so its view 
            // has been created at this point:
            DB.TableSchema inputSchema = db.getTableSchema(getChild(0).getViewName());
            // Next, parse the list of new column names:
            List<String> columnNames = parseColumnNames(_columns);
            if (inputSchema.getColNames().size() != columnNames.size()) {
                throw new ValidateException("renaming an incorrect number of columns", this);
            }
            String viewDef = "SELECT ";
            for (int i=0; i<columnNames.size(); i++) {
                if (i>0) viewDef += ", ";
                viewDef += "\"" + inputSchema.getColNames().get(i) +
                    "\" AS " + columnNames.get(i);
            }
            viewDef += " FROM " + getChild(0).getViewName();
            return viewDef;
        } else {
            return "SELECT * FROM " + getChild(0).getViewName();
        }
    }
    public String genViewCreateStatement(DB db)
        throws SQLException, ValidateException {
        if (db.getDriverName().equals("org.sqlite.JDBC")) {
            // See comments in genViewDef(DB):
            return "CREATE VIEW " + _viewName + " AS " + genViewDef(db);
        } else {
            return "CREATE VIEW " + _viewName + "(" + _columns + ") AS " +
                genViewDef(db);
        }
    }
    public String toPrintString() {
        return "\\rename_{" + _columns + "}";
    }
}
