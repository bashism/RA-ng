package edu.duke.ra.core.operator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.duke.ra.core.db.DB;
import edu.duke.ra.core.db.TableSchema;

public class Join extends RAXNode {
    protected String _condition;
    public Join(String condition, RAXNode input1, RAXNode input2) {
        super(new ArrayList<RAXNode>(Arrays.asList(input1, input2)));
        _condition = condition;
    }
    public String genViewDef(DB db)
        throws SQLException {
        if (_condition == null) {
            // Natural join:
            TableSchema input1Schema = db.getTableSchema(getChild(0).getViewName());
            TableSchema input2Schema = db.getTableSchema(getChild(1).getViewName());
            List<String> input1ColumnNames = input1Schema.getColNames();
            List<String> input2ColumnNames = input2Schema.getColNames();
            List<String> joinColumnNames = new ArrayList<String>();
            List<String> moreColumnNames = new ArrayList<String>();
            for (String col : input2ColumnNames) {
                if (input1ColumnNames.contains(col)) {
                    joinColumnNames.add(col);
                } else {
                    moreColumnNames.add(col);
                }
            }
            if (joinColumnNames.isEmpty()) {
                // Basically a cross product:
                return "SELECT * FROM " +
                    getChild(0).getViewName() + ", " + getChild(1).getViewName();
            } else {
                String viewDef = "SELECT ";
                for (int i=0; i<input1ColumnNames.size(); i++) {
                    if (i > 0) viewDef += ", ";
                    viewDef += "V1.\"" + input1ColumnNames.get(i) + "\"";
                }
                for (String col : moreColumnNames) {
                    viewDef += ", V2.\"" + col + "\"";
                }
                viewDef += " FROM " +
                    getChild(0).getViewName() + " AS V1, " +
                    getChild(1).getViewName() + " AS V2 WHERE ";
                for (int i=0; i<joinColumnNames.size(); i++) {
                    if (i > 0) viewDef += " AND ";
                    viewDef += "V1.\"" + joinColumnNames.get(i) +
                        "\"=V2.\"" + joinColumnNames.get(i) + "\"";
                }
                return viewDef;
            }
        } else {
            // Theta-join:
            return "SELECT * FROM " +
                getChild(0).getViewName() + ", " + getChild(1).getViewName() +
                " WHERE " + _condition;
        }
    }
    public String toPrintString() {
        return "\\join_{" + _condition + "}";
    }
}
