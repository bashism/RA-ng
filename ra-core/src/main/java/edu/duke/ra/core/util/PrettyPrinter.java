package edu.duke.ra.core.util;

import java.util.List;

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
}
