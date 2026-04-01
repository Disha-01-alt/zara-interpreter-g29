package main.java.zara.parser;

public class ParseException extends RuntimeException {
    private final int line;

    public ParseException(String message, int line) {
        super(message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}