package edu.duke.ra.core.operator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import edu.duke.ra.core.db.DB;

public class Select extends RAXNode {
    protected String _condition;
    public Select(String condition, RAXNode input) {
        super(new ArrayList<RAXNode>(Arrays.asList(input)));
        _condition = condition;
    }
    public String genViewDef(DB db)
        throws SQLException {
        return "SELECT * FROM " + getChild(0).getViewName() +
            " WHERE " + _condition;
    }
    public String toPrintString() {
        return "\\select_{" + _condition + "}";
    }
}
