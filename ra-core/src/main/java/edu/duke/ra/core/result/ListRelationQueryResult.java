package edu.duke.ra.core.result;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.duke.ra.core.RAException;

public class ListRelationQueryResult extends QueryResult {
    private static final String query = "\\list;\n";

    private List<RAException> errors;
    private List<String> relations;

    public ListRelationQueryResult(List<String> relations, List<RAException> errors) {
        this.relations = relations;
        this.errors = errors;
        makeResult();
    }

    @Override
    protected String makeQuery() {
        return query;
    }

    @Override
    protected JSONObject makeData() {
        JSONObject data = new JSONObject();
        JSONArray relationsJson = new JSONArray();
        for (String relation: relations) {
            relationsJson.put(relation);
        }
        data.put(dataRelationsKey, relationsJson);
        return data;
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
