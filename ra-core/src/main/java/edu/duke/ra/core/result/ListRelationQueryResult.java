package edu.duke.ra.core.result;

import java.util.List;

public class ListRelationQueryResult implements IQueryResult {
    private List<String> relations;

    public ListRelationQueryResult(List<String> relations) {
        this.relations = relations;
    }
    @Override
    public String toRawString() {
        StringBuilder builder = new StringBuilder();
        builder.append("-----\n");
        for (String relation: relations) {
            builder.append(relation + "\n");
        }
        builder.append("-----\n");
        builder.append("Total of " + relations.size() + " table(s) found.\n\n");
        return builder.toString();
    }

    @Override
    public String toJsonString() {
        return "{}";
    }

    @Override
    public boolean quit() {
        return false;
    }

}
