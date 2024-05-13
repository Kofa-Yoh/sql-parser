package org.kotkina;

import org.kotkina.errors.InvalidQueryException;
import org.kotkina.models.Query;
import org.kotkina.services.QueryService;

import java.util.Scanner;

public class Main {

    private static final String SCANNER_INPUT_TEXT = "\nEnter a SELECT query. Use a semicolon to end the query.\nTo exit, enter `0` or `exit`.";
    private static final QueryService queryService = new QueryService();

    public static void main(String[] args) {
        StringBuilder query = new StringBuilder();

        try (Scanner in = new Scanner(System.in)) {
            System.out.println(SCANNER_INPUT_TEXT);

            while (true) {
                String line = in.nextLine();

                if (line.equals("0") || line.equalsIgnoreCase("exit")) {
                    break;
                }

                query.append(line);
                if (line.contains(";")) {
                    parseAndPrint(query.toString());
                    query.setLength(0);
                    System.out.println(SCANNER_INPUT_TEXT);
                } else {
                    query.append(" ");
                }
            }
        }
    }

    private static void parseAndPrint(String query) {
        System.out.println("\nYour query:");
        System.out.println(query);
        try {
            Query result = queryService.parse(query);
            System.out.println(result);
        } catch (InvalidQueryException ex) {
            System.out.println(ex.getMessage());
        }
    }
}