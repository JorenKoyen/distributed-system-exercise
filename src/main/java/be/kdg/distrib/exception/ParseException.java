package be.kdg.distrib.exception;

public class ParseException extends RuntimeException {
    private final Exception exception;

    public ParseException(String message, Exception exception) {
        super(message);
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
