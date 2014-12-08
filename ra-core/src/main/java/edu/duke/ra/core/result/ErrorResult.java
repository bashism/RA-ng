package edu.duke.ra.core.result;

import java.util.List;

import org.json.JSONObject;

import edu.duke.ra.core.RAException;

public class ErrorResult extends QueryResult {
    private String query;
    private List<RAException> errors;

    public ErrorResult(String query, List<RAException> errors) {
        this.query = query;
        this.errors = errors;
    }

    @Override
    protected String makeQuery() {
        return query;
    }

    @Override
    protected JSONObject makeData() {
        return new JSONObject();
    }

    @Override
    protected List<RAException> makeErrors() {
        return errors;
    }

    @Override
    protected boolean makeQuit() {
        return false;
    }

}
