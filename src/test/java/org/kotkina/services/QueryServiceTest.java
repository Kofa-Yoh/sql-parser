package org.kotkina.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kotkina.errors.InvalidQueryException;
import org.kotkina.models.*;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class QueryServiceTest {

    private final QueryService queryService = new QueryService();

    @Test
    void parseSelectQueryWithAllKeywords() {
        String input = """
                SELECT author.name, count(book.id), sum(book.cost)\s
                FROM author\s
                LEFT JOIN book ON (author.id = book.author_id)\s
                WHERE book.cost >= 100\s
                GROUP BY author.name\s
                HAVING COUNT(*) > 1 AND SUM(book.cost) > 500\s
                LIMIT 10\s
                OFFSET 2;""";
        SelectQuery query = (SelectQuery) queryService.parse(input);

        assertAll(
                () -> assertEquals(query.getColumns().size(), 3),
                () -> assertEquals(query.getFromSources().size(), 1),
                () -> assertEquals(query.getJoins().size(), 1),
                () -> assertEquals(query.getWhereClauses().size(), 1),
                () -> assertEquals(query.getGroupByColumns().size(), 1),
                () -> assertEquals(query.getHavingClauses().size(), 2),
                () -> assertEquals(query.getLimit(), 10),
                () -> assertEquals(query.getOffset(), 2)
        );
    }

    @Test
    void parseSelectQueryWithSomeKeywords() {
        String input = "SELECT * FROM book";
        SelectQuery query = (SelectQuery) queryService.parse(input);

        assertAll(
                () -> assertEquals(query.getColumns().size(), 1),
                () -> assertEquals(query.getColumns().get(0).getName(), "*"),
                () -> assertEquals(query.getFromSources().size(), 1),
                () -> assertEquals(query.getFromSources().get(0).getTable(), "book"),
                () -> assertNull(query.getJoins()),
                () -> assertNull(query.getWhereClauses()),
                () -> assertNull(query.getGroupByColumns()),
                () -> assertNull(query.getHavingClauses()),
                () -> assertNull(query.getLimit()),
                () -> assertNull(query.getOffset())
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", "  ", "\t", "\n", "select"})
    void parseWrongSelectQueryWithException(String query) {
        assertThrows(InvalidQueryException.class, () -> queryService.parse(query));
    }

    @Test
    void generateColumns() {
        List<Column> columns1 = queryService.generateColumns(" author.name, count(book.id) count_alias, sum(book.cost) as s ");
        List<Column> columns2 = queryService.generateColumns(" * ");
        List<Column> columns3 = queryService.generateColumns(null);
        assertAll(
                () -> assertEquals(columns1.get(0).getName(), "author.name"),
                () -> assertNull(columns1.get(0).getAlias()),
                () -> assertEquals(columns1.get(1).getName(), "count(book.id)"),
                () -> assertEquals(columns1.get(1).getAlias(), "count_alias"),
                () -> assertEquals(columns1.get(2).getName(), "sum(book.cost)"),
                () -> assertEquals(columns1.get(2).getAlias(), "s"),

                () -> assertEquals(columns2.get(0).getName(), "*"),

                () -> assertNull(columns3)
        );
    }

    @Test
    void generateFromSources() {
        List<Source> sources1 = queryService.generateFromSources("A as author,B,C as \"customer\"");
        List<Source> sources2 = queryService.generateFromSources("  book b");
        List<Source> sources3 = queryService.generateFromSources(null);
        assertAll(
                () -> assertEquals(sources1.get(0).getTable(), "A"),
                () -> assertEquals(sources1.get(0).getAlias(), "author"),
                () -> assertEquals(sources1.get(1).getTable(), "B"),
                () -> assertNull(sources1.get(1).getAlias()),
                () -> assertEquals(sources1.get(2).getTable(), "C"),
                () -> assertEquals(sources1.get(2).getAlias(), "\"customer\""),

                () -> assertEquals(sources2.get(0).getTable(), "book"),
                () -> assertEquals(sources2.get(0).getAlias(), "b"),

                () -> assertNull(sources3)
        );
    }

    @Test
    void generateJoins() {
        Join join1 = queryService.generateJoin("JOIN book ON (author.id = book.author_id)", "LEFT");
        Join join2 = queryService.generateJoin("JOIN book2 on author.id = book2.author_id", "RIGHT");
        Join join3 = queryService.generateJoin("JOIN book3 ON author.id = book3.author_id", "INNER");
        Join join4 = queryService.generateJoin("join book4 ON author.id = book4.author_id", "FULL");

        assertAll(
                () -> assertEquals(join1.getSource().getTable(), "book"),
                () -> assertEquals(join1.getType(), "LEFT JOIN"),
                () -> assertEquals(join1.getConditions().get(0).getCondition(), "(author.id = book.author_id)"),
                () -> assertEquals(join2.getSource().getTable(), "book2"),
                () -> assertEquals(join2.getType(), "RIGHT JOIN"),
                () -> assertEquals(join2.getConditions().get(0).getCondition(), "author.id = book2.author_id"),
                () -> assertEquals(join3.getSource().getTable(), "book3"),
                () -> assertEquals(join3.getType(), "INNER JOIN"),
                () -> assertEquals(join3.getConditions().get(0).getCondition(), "author.id = book3.author_id"),
                () -> assertEquals(join4.getSource().getTable(), "book4"),
                () -> assertEquals(join4.getType(), "FULL JOIN"),
                () -> assertEquals(join4.getConditions().get(0).getCondition(), "author.id = book4.author_id")
        );
    }

    @Test
    void generateWhereClauses() {
        List<Clause> clauses1 = queryService.generateClauses("a.name LIKE \"% Alexandr %\" AND exists (select 1 from book b where b.author_id = a.id)");
        List<Clause> clauses2 = queryService.generateClauses(null);
        assertAll(
                () -> assertEquals(clauses1.get(0).getCondition(), "a.name LIKE \"% Alexandr %\""),
                () -> assertNull(clauses1.get(0).getOperator()),
                () -> assertEquals(clauses1.get(1).getCondition(), "exists (select 1 from book b where b.author_id = a.id)"),
                () -> assertEquals(clauses1.get(1).getOperator(), "AND"),

                () -> assertNull(clauses2)
        );
    }

    @Test
    void generateHavingClauses() {
        List<Clause> clauses1 = queryService.generateClauses("SUM(book.cost) > 500");
        List<Clause> clauses2 = queryService.generateClauses("SUM(book.cost) > 500 OR count(*) > 1");
        List<Clause> clauses3 = queryService.generateClauses(null);
        assertAll(
                () -> assertEquals(clauses1.get(0).getCondition(), "SUM(book.cost) > 500"),
                () -> assertNull(clauses1.get(0).getOperator()),

                () -> assertEquals(clauses2.get(0).getCondition(), "SUM(book.cost) > 500"),
                () -> assertNull(clauses2.get(0).getOperator()),
                () -> assertEquals(clauses2.get(1).getCondition(), "count(*) > 1"),
                () -> assertEquals(clauses2.get(1).getOperator(), "OR"),

                () -> assertNull(clauses3)
        );
    }

    @Test
    void generateGroupByColumns() {
        List<String> strings1 = queryService.generateGroupByColumns(" a.name , b.title ");
        List<String> strings2 = queryService.generateGroupByColumns(null);
        assertAll(
                () -> assertTrue(strings1.containsAll(List.of("b.title", "a.name"))),

                () -> assertNull(strings2)
        );
    }

    @Test
    void generateSortColumns() {
        List<Sort> sorts1 = queryService.generateSortColumns("author.name DESC    , book.id , book.cost ASC");
        List<Sort> sorts2 = queryService.generateSortColumns(null);
        assertAll(
                () -> assertEquals(sorts1.get(0).getExpression(), "author.name"),
                () -> assertTrue(sorts1.get(0).isDesc()),
                () -> assertEquals(sorts1.get(1).getExpression(), "book.id"),
                () -> assertFalse(sorts1.get(1).isDesc()),
                () -> assertEquals(sorts1.get(2).getExpression(), "book.cost"),
                () -> assertFalse(sorts1.get(2).isDesc()),

                () -> assertNull(sorts2)
        );
    }

    @Test
    void generateLimit() {
        Integer limit1 = queryService.generateLimit(" 20 ");
        Integer limit2 = queryService.generateLimit("all");
        Integer limit3 = queryService.generateLimit(null);
        assertAll(
                () -> assertEquals(limit1, 20),
                () -> assertNull(limit2),
                () -> assertNull(limit3)
        );
    }

    @Test
    void generateOffset() {
        Integer offset1 = queryService.generateOffset(" 20 ");
        Integer offset2 = queryService.generateOffset(null);
        assertAll(
                () -> assertEquals(offset1, 20),
                () -> assertNull(offset2)
        );
    }

    @Test
    void getExpressionWithAlias() {
        String input1 = "book b";
        String input2 = "book";
        String input3 = "(SELECT * FROM book) b";

        assertAll(
                () -> assertThat(queryService.getExpressionWithAlias(input1), is(new String[]{"book", "b"})),
                () -> assertThat(queryService.getExpressionWithAlias(input2), is(new String[]{"book", null})),
                () -> assertThat(queryService.getExpressionWithAlias(input3), is(new String[]{"(SELECT * FROM book)", "b"}))
        );
    }

    @Test
    void getSelectValuesWithAlias() {
        String input1 = "*";
        String input2 = "a.*";
        String input3 = "public.author.id";
        String input4 = "public.author.id id";
        String input5 = "sum(*) as s";

        assertAll(
                () -> assertThat(queryService.getSelectValuesWithAlias(input1), is(new String[]{"*", null})),
                () -> assertThat(queryService.getSelectValuesWithAlias(input2), is(new String[]{"a.*", null})),
                () -> assertThat(queryService.getSelectValuesWithAlias(input3), is(new String[]{"public.author.id", null})),
                () -> assertThat(queryService.getSelectValuesWithAlias(input4), is(new String[]{"public.author.id", "id"})),
                () -> assertThat(queryService.getSelectValuesWithAlias(input5), is(new String[]{"sum(*)", "s"}))
        );
    }
}