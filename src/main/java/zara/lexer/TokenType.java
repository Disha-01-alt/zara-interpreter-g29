package zara.lexer;

/**
 * Every distinct kind of token the ZARA language can produce.
 *
 * Rules:
 *  - Keywords (SET, SHOW, WHEN, LOOP) are their own types — never IDENTIFIER.
 *  - ASSIGN is a single '='.  EQUALS is '=='.  The Tokenizer must peek ahead
 *    at the next character to decide which one to emit.
 *  - NEWLINE is significant — emitted for every \n in the source.
 *  - INDENT is emitted once when indentation increases after a block header (:).
 *  - DEDENT is emitted once when indentation returns to a previous level.
 *    The Parser uses INDENT/DEDENT as unambiguous block start/end markers.
 *  - EOF is always the last token in the list, exactly once.
 */
public enum TokenType {

    // ── Literals ──────────────────────────────────────────────────────────────
    NUMBER,        // e.g.  10   3   85   3.14
    STRING,        // e.g.  "Sitare"   "Hello from ZARA"
    IDENTIFIER,    // e.g.  x   y   result   score   i   name

    // ── ZARA Keywords ─────────────────────────────────────────────────────────
    SET,           // set
    SHOW,          // show
    WHEN,          // when
    LOOP,          // loop

    // ── Arithmetic Operators ──────────────────────────────────────────────────
    PLUS,          // +
    MINUS,         // -
    STAR,          // *
    SLASH,         // /

    // ── Grouping ──────────────────────────────────────────────────────────────
    LPAREN,        // (   allows expressions like (a + b) * 2
    RPAREN,        // )

    // ── Comparison & Assignment ───────────────────────────────────────────────
    ASSIGN,        // =   (single equals — variable assignment)
    EQUALS,        // ==  (double equals — equality comparison)
    GREATER,       // >
    LESS,          // <

    // ── Structure ─────────────────────────────────────────────────────────────
    COLON,         // :   (ends a when/loop header line)
    NEWLINE,       // \n  (statement separator)
    INDENT,        // emitted when indentation level increases (block start)
    DEDENT,        // emitted when indentation level decreases (block end)
    EOF            // end of token stream — always the last token
}