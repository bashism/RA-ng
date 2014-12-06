package edu.duke.ra.core.operator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import edu.duke.ra.core.db.DB;

public class Project extends RAXNode {
    protected String _columns;
    public Project(String columns, RAXNode input) {
        super(new ArrayList<RAXNode>(Arrays.asList(input)));
        _columns = columns;
    }
    public String genViewDef(DB db)
        throws SQLException {
        return "SELECT DISTINCT " + _columns + " FROM " + getChild(0).getViewName();
    }
    public String toPrintString() {
        return "\\project_{" + _columns + "}";
    }
}
