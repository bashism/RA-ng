package edu.duke.ra.core.result;

import java.util.List;

import org.json.JSONObject;

import edu.duke.ra.core.RAException;
import edu.duke.ra.core.db.DB;

public class RawStringQueryResult extends QueryResult {
    private String query;
    private String data;
    private List<RAException> errors;

    public RawStringQueryResult(String query, String data, List<RAException> errors) {
        this.query = query;
        this.data = data;
        this.errors = errors;
        makeResult();
    }

    @Override
    protected String makeQuery() {
        return query;
    }

    @Override
    protected JSONObject makeData() {
        JSONObject dataJson = new JSONObject();
        dataJson.put(dataTextKey, data);
        return dataJson;
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
