package zara.lexer;

/**
 * One piece of ZARA source code — an immutable value object.
 *
 * Every token carries three facts:
 *   type  — what kind of thing it is (from TokenType enum)
 *   value — the exact text from the source file
 *   line  — which line it came from (1-based)
 *
 * Immutability contract:
 *   All fields are final and set once in the constructor.
 *   There are no setters. Tokens are never modified after creation.
 *
 * The class is final because tokens are pure value objects — there is
 * no meaningful subtype of "a token". Marking it final also lets the
 * JVM inline getter calls.
 */
public final class Token {

    private final TokenType type;
    private final String    value;
    private final int       line;

    /**
     * @param type  the category of this token
     * @param value the raw text from the source (e.g. "set", "42", "+")
     * @param line  the 1-based line number where this token appears
     */
    public Token(TokenType type, String value, int line) {
        this.type  = type;
        this.value = value;
        this.line  = line;
    }

    /** Returns what kind of token this is. */
    public TokenType getType() {
        return type;
    }

    /**
     * Returns the raw text from the source file.
     * For STRING tokens this is the content WITHOUT the surrounding quotes.
     * For NUMBER tokens this is the digit string, e.g. "10" or "3".
     */
    public String getValue() {
        return value;
    }

    /** Returns the 1-based line number this token was found on. */
    public int getLine() {
        return line;
    }

    /**
     * Human-readable representation — used in error messages and debugging.
     * Example output:  Token(NUMBER, "42", line=3)
     */
    @Override
    public String toString() {
        return String.format("Token(%s, \"%s\", line=%d)", type, value, line);
    }
}
