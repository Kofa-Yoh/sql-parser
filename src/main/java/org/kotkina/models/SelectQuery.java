package org.kotkina.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SelectQuery implements Query {
    private List<Column> columns;
    private List<Source> fromSources;
    private List<Join> joins;
    private List<Clause> whereClauses;
    private List<String> groupByColumns;
    private List<Clause> havingClauses;
    private List<Sort> sortColumns;
    private Integer limit;
    private Integer offset;

    public void addJoin(Join join) {
        if (joins == null) {
            joins = new ArrayList<>();
        }
        joins.add(join);
    }

    @Override
    public String toString() {
        return "\n{" +
                "\n\"columns\": " + columns +
                (fromSources == null ? "" : ",\n\"fromSources\": " + fromSources) +
                (joins == null ? "" : ",\n\"joins\": " + joins) +
                (whereClauses == null ? "" : ",\n\"whereClauses\": " + whereClauses) +
                (groupByColumns == null ? "" : ",\n\"groupByColumns\": " + groupByColumns) +
                (havingClauses == null ? "" : ",\n\"havingClauses\": " + havingClauses) +
                (sortColumns == null ? "" : ",\n\"sortColumns\": " + sortColumns) +
                (limit == null ? "" : ",\n\"limit\": " + limit) +
                (offset == null ? "" : ",\n\"offset\": " + offset) +
                "\n}";
    }
}
