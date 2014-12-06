package edu.duke.ra.core.query;

import antlr.collections.AST;
import edu.duke.ra.core.db.DB;
import edu.duke.ra.core.result.IQueryResult;

public class ListQuery extends DatabaseQuery{

    public ListQuery(DB database) {
        super(database);
    }

    @Override
    public IQueryResult query(AST query) {
        return null;
    }

}
