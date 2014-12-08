package edu.duke.ra.core.result;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.duke.ra.core.RAException;
import edu.duke.ra.core.util.PrettyPrinter;

/**
 * A result returning a list of tuples
 *
 */
public class StandardQueryResult extends QueryResult {
    private static final String schemaKey = "schema";
    private static final String schemaNameKey = "name";
    private static final String schemaTypeKey = "type";
    private static final String entriesKey = "entries";
    
    List<Column> outputSchema = new ArrayList<>();
    List<List<String>> results = new ArrayList<>();
    private List<RAException> errors;
    private String query;

    public StandardQueryResult(ResultSet resultSet, List<RAException> errors, String raQueryString, String sqlQueryString) {
        this.query = raQueryString;
        this.errors = new ArrayList<>(errors);
        if (resultSet == null) {
            outputSchema = new ArrayList<>();
        }
        try {
            outputSchema = processColumnSchema(resultSet);
            results = processResultSet(resultSet);
        } catch (SQLException exception) {
            this.errors.add(new RAException(
                    "SQLException",
                    "An error occurred creating a standard query result",
                    "RA query: " + raQueryString + "\n"
                            + "SQL query: " + sqlQueryString + "\n",
                    exception));
            outputSchema = new ArrayList<>();
            results = new ArrayList<>();
        }
        makeResult();
    }

    List<Column> processColumnSchema(ResultSet resultSet) throws SQLException {
        List<Column> outputSchema = new ArrayList<>();
        ResultSetMetaData metadata = resultSet.getMetaData();
        for (int i = 1; i <= metadata.getColumnCount(); i++){
            String columnName = metadata.getColumnName(i);
            String columnType = metadata.getColumnTypeName(i);
            outputSchema.add(new Column(columnName, columnType));
        }
        return outputSchema;
    }

    List<List<String>> processResultSet(ResultSet resultSet) throws SQLException {
        List<List<String>> results = new ArrayList<>();
        while (resultSet.next()){
            List<String> resultRow = new ArrayList<>();
            for (int i = 1; i <= outputSchema.size(); i++) {
                String result = convertSQLTypeToString(resultSet, i);
                resultRow.add(result == null ? "<NULL>" : result);
            }
            results.add(resultRow);
        }
        return results;
    }

    String convertSQLTypeToString(ResultSet resultSet, int column) throws SQLException {
        return resultSet.getString(column);
    }

    @Override
    protected String makeQuery() {
        return query;
    }

    @Override
    protected JSONObject makeData() {
        JSONObject data = new JSONObject();
        JSONObject tuples = new JSONObject();
        tuples.put(schemaKey, makeSchemaJson());
        tuples.put(entriesKey, makeEntries());
        data.put(dataTuplesKey, tuples);
        return data;
    }
    
    private JSONArray makeSchemaJson(){
        JSONArray schema = new JSONArray();
        for (Column column: outputSchema) {
            JSONObject schemaEntry = new JSONObject();
            schemaEntry.put(schemaNameKey, column.name());
            schemaEntry.put(schemaTypeKey, column.type());
            schema.put(schemaEntry);
        }
        return schema;
    }

    private JSONArray makeEntries(){
        JSONArray entries = new JSONArray();
        for (List<String> row: results) {
            JSONArray rowJson = new JSONArray();
            for (String column: row) {
                rowJson.put(column);
            }
            entries.put(rowJson);
        }
        return entries;
    }

    @Override
    protected List<RAException> makeErrors() {
        return errors;
    }

    @Override
    protected boolean makeQuit() {
        return false;
    }

    public String toRawString() {
        return PrettyPrinter.printTuples(outputSchema, results);
    }
}
