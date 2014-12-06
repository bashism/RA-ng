package edu.duke.ra.core.operator;

import java.sql.SQLException;
import java.util.ArrayList;

import edu.duke.ra.core.db.DB;

public class Table extends RAXNode{
    protected String _tableName;
    public Table(String tableName) {
        super(new ArrayList<RAXNode>());
        _tableName = tableName;
    }
    public String genViewDef(DB db)
        throws SQLException {
        return "SELECT DISTINCT * FROM " + _tableName;
    }
    public String toPrintString() {
        return _tableName;
    }
}
