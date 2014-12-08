package edu.duke.ra.core.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.duke.ra.core.result.Column;

public class PrettyPrinter {
    public static String printRelations(List<String> relations){
        StringBuilder builder = new StringBuilder();
        builder.append("-----\n");
        for (String relation: relations) {
            builder.append(relation + "\n");
        }
        builder.append("-----\n");
        builder.append("Total of " + relations.size() + " table(s) found.\n\n");
        return builder.toString();
    }
    public static String printTuples(List<Column> outputSchema, List<List<String>> results) {
        StringBuilder output = new StringBuilder();
        appendSchema(output, outputSchema);
        appendResults(output, results);
        appendSummary(output, results);
        return output.toString();
    }

    static void appendSchema(StringBuilder builder, List<Column> outputSchema) {
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

    static void appendResults(StringBuilder builder, List<List<String>> results) {
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

    static void appendSummary(StringBuilder builder, List<List<String>> results) {
        builder.append("-----\n");
        builder.append("Total number of rows: " + results.size() + "\n\n");
    }
    public static List<Column> createSchemaFromJson(String json) {
        JSONObject resultJson = new JSONObject(json);
        JSONArray schemaJson = resultJson.getJSONObject("data")
                .getJSONObject("tuples")
                .getJSONArray("schema");
        List<Column> outputSchema = new ArrayList<>();
        for (int i = 0; i < schemaJson.length(); i++){
            JSONObject schemaDef = schemaJson.getJSONObject(i);
            outputSchema.add(new Column(schemaDef.getString("name"), schemaDef.getString("type")));
        }
        return outputSchema;
    }
    public static List<List<String>> createTuplesFromJson(String json) {
        JSONObject resultJson = new JSONObject(json);
        JSONArray tuplesJson = resultJson.getJSONObject("data")
                .getJSONObject("tuples")
                .getJSONArray("entries");
        List<List<String>> tuples = new ArrayList<>();
        for (int i = 0; i < tuplesJson.length(); i++){
            List<String> row = new ArrayList<>();
            for (int j = 0; j < tuplesJson.getJSONArray(0).length(); j++){
                String columnEntry = tuplesJson.getJSONArray(i).getString(j);
                row.add(columnEntry);
            }
            tuples.add(row);
        }
        return tuples;
    }
    public static List<String> createRelationsFromJson(String json) {
        JSONObject resultJson = new JSONObject(json);
        JSONArray relationsJson = resultJson.getJSONObject("data")
                .getJSONArray("relations");
        List<String> relations = new ArrayList<>();
        for (int i = 0; i < relationsJson.length(); i++) {
            String relation = relationsJson.getString(i);
            relations.add(relation);
        }
        return relations;
    }
}
