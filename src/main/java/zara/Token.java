package zara;

public class Token {
    private final TokenType type;
    private final String value;
    private final int line;

    public Token(TokenType type, String value, int line) {
        // TODO: Store the three fields
        this.type = type;
        this.value = value;
        this.line = line;
    }

    public TokenType getType() { /* TODO */ return type; }
    public String getValue() { /* TODO */ return value; }
    public int getLine() { /* TODO */ return line; }

    @Override
    public String toString() {
        return type + "(" + value + ") at line " + line;
    }
}
