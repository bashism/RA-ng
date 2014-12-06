package edu.duke.ra.core.query;

import edu.duke.ra.core.db.DB;

public abstract class DatabaseQuery implements IQuery{
    protected DB database;
    public DatabaseQuery(DB database) {
        this.database = database;
    }
}
