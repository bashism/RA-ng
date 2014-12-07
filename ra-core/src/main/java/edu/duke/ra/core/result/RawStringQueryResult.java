package edu.duke.ra.core.result;

import edu.duke.ra.core.db.DB;

public class RawStringQueryResult implements IQueryResult {
    private String string;
    public RawStringQueryResult(String string) {
        this.string = string;
    }
    @Override
    public String toRawString() {
        return string;
    }

    @Override
    public String toJsonString() {
        return "{}";
    }

    @Override
    public boolean quit() {
        return false;
    }

}
