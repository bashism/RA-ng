package edu.duke.ra.core;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class QueryResult {
    List<Column> outputSchema = new ArrayList<>();
    List<List<String>> results = new ArrayList<>();
    String error;

    public QueryResult(ResultSet resultSet) throws SQLException{
        outputSchema = processColumnSchema(resultSet);
        results = processResultSet(resultSet);
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

    public String toRawString(){
        StringBuilder output = new StringBuilder();
        appendSchema(output);
        appendResults(output);
        appendSummary(output);
        return output.toString();
    }

    void appendSchema(StringBuilder builder) {
        builder.append("Output schema: (");
        for (int i = 0; i < outputSchema.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(outputSchema.get(i).name() + " " + outputSchema.get(i).type());
        }
        builder.append(")\n");
        builder.append("-----\n");
    }

    void appendResults(StringBuilder builder) {
        for (List<String> row: results) {
            for (int i = 0; i < row.size(); i++) {
                if (i > 0) {
                    builder.append("|");
                }
                builder.append(row.get(i));
            }
            builder.append("\n");
        }
    }

    void appendSummary(StringBuilder builder) {
        builder.append("-----\n");
        builder.append("Total number of rows: " + results.size() + "\n\n");
    }

    public String toJsonString(){
        return "";
    }
}
