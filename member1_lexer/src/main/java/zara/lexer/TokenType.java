package zara.lexer;

/**
 * Every distinct kind of token the ZARA language can produce.
 *
 * Rules:
 *  - Keywords (SET, SHOW, WHEN, LOOP) are their own types — never IDENTIFIER.
 *  - ASSIGN is a single '='.  EQUALS is '=='.  The Tokenizer must peek ahead
 *    at the next character to decide which one to emit.
 *  - NEWLINE is significant — the Parser uses it to detect statement boundaries
 *    and the end of indented blocks.
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

    // ── Comparison & Assignment ───────────────────────────────────────────────
    ASSIGN,        // =   (single equals — variable assignment)
    EQUALS,        // ==  (double equals — equality comparison)
    GREATER,       // >
    LESS,          // <

    // ── Structure ─────────────────────────────────────────────────────────────
    COLON,         // :   (ends a when/loop header line)
    NEWLINE,       // \n  (statement separator; also marks block boundaries)
    EOF            // end of token stream — always the last token
}
