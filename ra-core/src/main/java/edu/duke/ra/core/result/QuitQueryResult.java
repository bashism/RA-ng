package edu.duke.ra.core.result;

public class QuitQueryResult implements IQueryResult {

    @Override
    public String toRawString() {
        return "";
    }

    @Override
    public String toJsonString() {
        return "{}";
    }

    @Override
    public boolean quit() {
        return true;
    }
}
