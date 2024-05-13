package org.kotkina.models;

import java.util.Arrays;
import java.util.List;

public enum Keyword {

    SELECT, FROM, INNER, LEFT, RIGHT, FULL, WHERE, GROUP, HAVING, ORDER, LIMIT, OFFSET;

    public static List<String> getList() {
        return Arrays.stream(Keyword.values())
                .map(Enum::name)
                .toList();
    }
}
