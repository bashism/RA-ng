package edu.duke.ra.core.operator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import edu.duke.ra.core.db.DB;

public class Cross extends RAXNode{
    public Cross(RAXNode input1, RAXNode input2) {
        super(new ArrayList<RAXNode>(Arrays.asList(input1, input2)));
    }
    public String genViewDef(DB db)
        throws SQLException {
        return "SELECT * FROM " +
            getChild(0).getViewName() + ", " + getChild(1).getViewName();
    }
    public String toPrintString() {
        return "\\cross";
    }
}
