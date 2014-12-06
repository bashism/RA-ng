package edu.duke.ra.core.operator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import edu.duke.ra.core.ValidateException;
import edu.duke.ra.core.db.DB;

public class Intersect extends RAXNode {
    public Intersect(RAXNode input1, RAXNode input2) {
        super(new ArrayList<RAXNode>(Arrays.asList(input1, input2)));
    }
    public String genViewDef(DB db)
        throws SQLException, ValidateException {
        if (db.getDriverName().equals("com.mysql.jdbc.Driver")) {
            // MySQL doesn't support INTERSECT, so we need a workaround.
            // First, get the input schema of the children, which
            // should have already been validated so their views
            // have been created at this point:
            DB.TableSchema input1Schema = db.getTableSchema(getChild(0).getViewName());
            DB.TableSchema input2Schema = db.getTableSchema(getChild(1).getViewName());
            if (input1Schema.getColNames().size() !=
                input2Schema.getColNames().size()) {
                throw new ValidateException("intersecting relations with different numbers of columns", this);
            }
            String viewDef = "SELECT DISTINCT * FROM " + getChild(0).getViewName() +
                " WHERE EXISTS (SELECT * FROM " + getChild(1).getViewName() +
                " WHERE ";
            for (int i=0; i<input1Schema.getColNames().size(); i++) {
                if (i>0) viewDef += " AND ";
                viewDef += getChild(0).getViewName() + ".\"" +
                    input1Schema.getColNames().get(i) + "\"=" +
                    getChild(1).getViewName() + ".\"" +
                    input2Schema.getColNames().get(i) + "\"";
            }
            viewDef += ")";
            return viewDef;
        } else {
            return "SELECT * FROM " + getChild(0).getViewName() +
                " INTERSECT SELECT * FROM " + getChild(1).getViewName();
        }
    }
    public String toPrintString() {
        return "\\intersect";
    }
}
