package zara.lexer;

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
    LESS_THAN,    // <
    EQUAL_EQUAL,  // ==
    COLON,        // :

    // Special
    NEWLINE, EOF
}
