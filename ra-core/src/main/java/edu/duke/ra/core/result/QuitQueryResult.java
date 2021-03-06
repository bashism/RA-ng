package edu.duke.ra.core.result;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import edu.duke.ra.core.RAException;

public class QuitQueryResult extends QueryResult {
    private static final String quitMessage = "Bye!\n\n";
    private final String query;

    public QuitQueryResult(String query){
        this.query = query;
        makeResult();
    }

    @Override
    protected String makeQuery() {
        return query;
    }
    @Override
    protected JSONObject makeData() {
        JSONObject quitData = new JSONObject();
        quitData.put(dataTextKey, quitMessage);
        return quitData;
    }
    @Override
    protected List<RAException> makeErrors() {
        return new ArrayList<>();
    }
    @Override
    protected boolean makeQuit() {
        return true;
    }
}
