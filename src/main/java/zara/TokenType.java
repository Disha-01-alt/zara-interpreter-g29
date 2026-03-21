package zara;

public enum TokenType {
    // Literals
    NUMBER,      // 42, 3.14
    STRING,      // "hello"
    IDENTIFIER,  // variable names like x, result

    // Arithmetic Operators
    PLUS,        // +
    MINUS,       // -
    STAR,        // *
    SLASH,       // /

    // Comparison Operators
    GREATER,     // >
    LESS,        // <
    EQUAL_EQUAL, // ==

    // Symbols
    ASSIGN,      // =
    COLON,       // :
    LPAREN,      // (
    RPAREN,      // )

    // Keywords (ZARA-specific)
    SET,         // set
    SHOW,        // show
    WHEN,        // when
    LOOP,        // loop

    // Structural
    NEWLINE,     // end of line
    EOF          // end of file
}
