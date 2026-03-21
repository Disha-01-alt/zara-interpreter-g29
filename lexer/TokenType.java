package lexer;

public enum TokenType {
    // Keywords
    SET, SHOW, WHEN, LOOP,

    // Types
    IDENTIFIER, NUMBER, STRING,

    // Operators & Symbols
    ASSIGN,       // =
    PLUS,         // +
    MINUS,        // -
    MULTIPLY,     // *
    DIVIDE,       // /
    GREATER_THAN, // >
    COLON,        // :

    // Special
    NEWLINE, EOF
}
