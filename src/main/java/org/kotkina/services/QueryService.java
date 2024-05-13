package org.kotkina.services;

import org.kotkina.errors.InvalidQueryException;
import org.kotkina.models.*;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.kotkina.models.Keyword.*;

public class QueryService {

    private static final String SELECT_REGEX = "\\s*(?<object>[\\p{Alnum}_]*\\([^()]+\\)|[\\p{Alnum}_.*]+)(?:\\s+\\bAS\\b)?(?<alias>\\s+(?:\"[^\"]+\"|[\\p{Alnum}_]+))?\\s*$";
    private static final String SOURCE_REGEX = "^\\s*(?<object>\\([^()]+\\)|[\\p{Alnum}_.]+)(?:\\s+\\bAS\\b)?(?<alias>\\s+(?:\"[^\"]+\"|[\\p{Alnum}_]+))?\\s*$";
    private static final String JOIN_REGEX = "\\s*\\bJOIN\\b\\s+(?<table>\\([^()]+\\)|[\\p{Alnum}_.]+)(?:\\s+\\bAS\\b)?(?<alias>\\s+(?:\"[^\"]+\"|[\\p{Alnum}_]+))?\\s+ON\\s+(?<clause>.+?)$";
    private static final String CLAUSE_REGEX = "\\s*(?<operator>\\b(AND|OR)\\b)?\\s*(?<clause>.*?(\\([^()]+?\\)|[^()]+?))(?=\\s*(\\b(AND|OR)\\b|$))";
    private static final Pattern SELECT_PATTERN = Pattern.compile(SELECT_REGEX, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SOURCE_PATTERN = Pattern.compile(SOURCE_REGEX, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern JOIN_PATTERN = Pattern.compile(JOIN_REGEX, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern CLAUSE_PATTERN = Pattern.compile(CLAUSE_REGEX, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final String ERROR_PATTERN = "Error in {0} clause: {1}";

    public Query parse(String query) {
        validateString(query);
        List<String> tokens = getTokens(query);

        return generateQuery(tokens);
    }

    private void validateString(String query) {
        if (query == null || query.isEmpty()) throw new InvalidQueryException();

        Pattern pattern = Pattern.compile("^[^;]+;?\\s*$");
        Matcher matcher = pattern.matcher(query);
        if (!matcher.find()) {
            throw new InvalidQueryException();
        }
    }

    private List<String> getTokens(String query) {
        Pattern pattern = Pattern.compile("[\\s\\v]*[^\\s(),;]*(?:\\([^()]+\\))*,?[\\s\\v]*");
        Matcher matcher = pattern.matcher(query);

        return matcher.results()
                .map(result -> result.group().trim())
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private Query generateQuery(List<String> tokens) {
        if (tokens == null || tokens.size() == 0) {
            throw new InvalidQueryException();
        }

        if ("SELECT".equalsIgnoreCase(tokens.get(0))) {
            return generateSelectQuery(tokens);
        }

        throw new InvalidQueryException();
    }

    private SelectQuery generateSelectQuery(List<String> tokens) {
        SelectQuery selectQuery = new SelectQuery();
        List<String> keywords = Keyword.getList();

        StringBuilder expression = new StringBuilder();
        Keyword currentKeyword = SELECT;
        Iterator<String> iterator = tokens.iterator();
        if (iterator.hasNext()) iterator.next();

        while (iterator.hasNext()) {
            String token = iterator.next();
            Optional<String> maybeKeyword = keywords.stream()
                    .filter(k -> k.equalsIgnoreCase(token))
                    .findFirst();

            if (maybeKeyword.isEmpty()) {
                if (expression.length() > 0) {
                    expression.append(" ");
                }
                expression.append(token);
                if (iterator.hasNext()) continue;
            }

            if (expression.length() == 0) {
                throw new InvalidQueryException(MessageFormat.format(ERROR_PATTERN, currentKeyword, ""));
            }

            switch (currentKeyword) {
                case SELECT -> selectQuery.setColumns(generateColumns(expression.toString()));
                case FROM -> selectQuery.setFromSources(generateFromSources(expression.toString()));
                case INNER, LEFT, RIGHT, FULL ->
                        selectQuery.addJoin(generateJoin(expression.toString(), currentKeyword.name()));
                case WHERE -> selectQuery.setWhereClauses(generateClauses(expression.toString()));
                case GROUP -> selectQuery.setGroupByColumns(generateGroupByColumns(expression.toString()));
                case HAVING -> selectQuery.setHavingClauses(generateClauses(expression.toString()));
                case ORDER -> selectQuery.setSortColumns(generateSortColumns(expression.toString()));
                case LIMIT -> selectQuery.setLimit(generateLimit(expression.toString()));
                case OFFSET -> selectQuery.setOffset(generateOffset(expression.toString()));
            }

            if (iterator.hasNext() && maybeKeyword.isPresent()) {
                currentKeyword = Keyword.valueOf(maybeKeyword.get());
            }
            expression.setLength(0);
        }

        if (selectQuery.getColumns() == null || selectQuery.getColumns().size() == 0) {
            throw new InvalidQueryException(MessageFormat.format(ERROR_PATTERN, "SELECT", expression));
        }
        return selectQuery;
    }

    protected List<Column> generateColumns(String expression) {
        if (expression == null || expression.isEmpty()) return null;

        return Arrays.stream(expression.trim().split(",(?![^(]*\\))"))
                .map(this::getSelectValuesWithAlias)
                .map(s -> {
                    if (s == null) {
                        throw new InvalidQueryException(MessageFormat.format(ERROR_PATTERN, "SELECT", expression));
                    }
                    return new Column(s[0], s[1]);
                })
                .toList();
    }

    protected List<Source> generateFromSources(String expression) {
        if (expression == null || expression.isEmpty()) return null;

        return Arrays.stream(expression.trim().split(",(?![^(]*\\))"))
                .map(this::getExpressionWithAlias)
                .map(s -> {
                    if (s == null) {
                        throw new InvalidQueryException(MessageFormat.format(ERROR_PATTERN, "FROM", expression));
                    }
                    return new Source(s[0], s[1]);
                })
                .toList();
    }

    protected Join generateJoin(String expression, String joinType) {
        if (expression == null || expression.isEmpty()) return null;

        Matcher matcher = JOIN_PATTERN.matcher(expression.trim());
        if (matcher.find()) {
            String conditions = matcher.group("clause");
            List<Clause> clauses = generateClauses(conditions);
            return new Join(joinType + " JOIN", new Source(matcher.group("table"), matcher.group("alias")), clauses);
        }

        throw new InvalidQueryException(MessageFormat.format(ERROR_PATTERN, "JOIN", joinType + " " + expression));
    }

    protected List<Clause> generateClauses(String expression) {
        if (expression == null || expression.isEmpty()) return null;

        Matcher matcher = CLAUSE_PATTERN.matcher(expression.trim());
        ArrayList<Clause> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(new Clause(matcher.group("clause"), matcher.group("operator")));
        }

        return list;
    }

    protected List<String> generateGroupByColumns(String expression) {
        if (expression == null || expression.isEmpty()) return null;

        if (expression.startsWith("BY")) {
            expression = expression.substring(2);
        }

        return Arrays.stream(expression.trim().split(",(?![^(]*\\))"))
                .map(String::trim)
                .toList();
    }

    protected List<Sort> generateSortColumns(String expression) {
        if (expression == null || expression.isEmpty()) return null;

        if (expression.startsWith("BY")) {
            expression = expression.substring(2);
        }

        return Arrays.stream(expression.trim().split(",(?![^(]*\\))"))
                .map(String::trim)
                .map(s -> {
                    int length = s.length();
                    return switch (s.substring(length - 4).toUpperCase()) {
                        case "DESC" -> new Sort(s.substring(0, length - 5), true);
                        case " ASC" -> new Sort(s.substring(0, length - 4), false);
                        default -> new Sort(s);
                    };
                })
                .toList();
    }

    protected Integer generateLimit(String expression) {
        if (expression == null || expression.isEmpty() || expression.equalsIgnoreCase("ALL")) return null;

        return Integer.parseInt(expression.trim());
    }

    protected Integer generateOffset(String expression) {
        if (expression == null || expression.isEmpty()) return null;

        return Integer.parseInt(expression.trim());
    }

    protected String[] getExpressionWithAlias(String string) {
        return getExpressionWithAlias(string, SOURCE_PATTERN);
    }

    protected String[] getSelectValuesWithAlias(String string) {
        return getExpressionWithAlias(string, SELECT_PATTERN);
    }

    protected String[] getExpressionWithAlias(String string, Pattern pattern) {
        Matcher matcher = pattern.matcher(string.trim());

        if (matcher.find()) {
            String object = matcher.group("object");
            String alias = matcher.group("alias");

            if (object == null || object.trim().isEmpty()) {
                return null;
            }

            if (alias == null) {
                return new String[]{object.trim(), null};
            }

            return new String[]{object.trim(), alias.trim()};
        } else {
            return null;
        }
    }
}
