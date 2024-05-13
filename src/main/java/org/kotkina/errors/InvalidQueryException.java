package org.kotkina.errors;

public class InvalidQueryException extends RuntimeException {

    public InvalidQueryException() {
        super("The query syntax is incorrect.");
    }

    public InvalidQueryException(String message) {
        super(message);
    }
}
