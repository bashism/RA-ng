package edu.duke.ra.core.result;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.duke.ra.core.RAException;

public abstract class QueryResult implements IQueryResult {
    private static final String queryKey = "query";
    private static final String dataKey = "data";
    private static final String errorsKey = "errors";
    private static final String quitKey = "quit";

    private static final String errorNameKey = "name";
    private static final String errorDescriptionKey = "description";
    private static final String errorDetailsKey = "details";
    private static final String errorMessageKey = "message";
    private static final String errorStackTraceKey = "stackTrace";

    protected static final String dataTextKey = "text";
    protected static final String dataRelationsKey = "relations";
    protected static final String dataTuplesKey = "tuples";

    private JSONObject result;

    /**
     * Create the JSON representation of the result. You MUST call this
     * in the subclasses' constructors
     */
    protected void makeResult() {
        this.result = new JSONObject();
        this.result.put(queryKey, makeQuery());
        this.result.put(dataKey, makeData());
        this.result.put(errorsKey, makeErrorJson(makeErrors()));
        this.result.put(quitKey, makeQuit());
    }

    protected abstract String makeQuery();
    protected abstract JSONObject makeData();
    protected abstract List<RAException> makeErrors();
    protected abstract boolean makeQuit();

    private JSONArray makeErrorJson(List<RAException> exceptions){
        JSONArray errors = new JSONArray();
        // add all the new fields
        /*   
    public String details(){
        return details;
    }
    public String message(){
        return message;
    }
    public String stackTrace(){
        return stackTrace;
    }*/
        for (RAException exception: exceptions) {
            JSONObject errorJson = new JSONObject();
            errorJson.put(errorNameKey, exception.name());
            errorJson.put(errorDescriptionKey, exception.description());
            errorJson.put(errorDetailsKey, exception.details());
            errorJson.put(errorMessageKey, exception.message());
            //errorJson.put(errorStackTraceKey, exception.stackTrace());
            errors.put(errorJson);
        }
        return errors;
    }

    @Override
    public String toJsonString() {
        return this.result.toString();
    }
}
