package zara.lexer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Stage 1 of the ZARA pipeline.
 *
 * Reads a raw source String character by character and produces a
 * flat List<Token>. The last token in the list is always EOF.
 *
 * Responsibilities:
 *   - Recognise every token type defined in TokenType
 *   - Track line numbers accurately
 *   - Strip quote characters from string literals
 *   - Distinguish '=' (ASSIGN) from '==' (EQUALS)
 *   - Emit INDENT / DEDENT tokens for block boundary detection
 *   - Reject unterminated string literals with a clear error
 *
 * INDENT / DEDENT logic (fixes review point 1):
 *   ZARA uses indentation to define blocks, exactly like Python.
 *   After every NEWLINE the Tokenizer measures the indentation of the
 *   next non-blank line (number of leading spaces/tabs).
 *
 *   - If indentation INCREASES  → emit one INDENT token
 *   - If indentation DECREASES  → emit one or more DEDENT tokens
 *                                  (one per level closed)
 *   - If indentation is the SAME → emit nothing extra
 *
 *   The current indentation stack starts at [0] (column 0 = top level).
 *
 * Example token stream for:
 *   when score > 50:       → WHEN IDENTIFIER GREATER NUMBER COLON NEWLINE
 *       show "Pass"        → INDENT SHOW STRING NEWLINE
 *   show "Done"            → DEDENT SHOW STRING NEWLINE EOF
 *
 * The Parser simply looks for INDENT to enter a block and DEDENT to leave.
 * It never has to inspect raw whitespace.
 *
 * This class does NOT:
 *   - Understand grammar or instruction structure (that is the Parser's job)
 *   - Evaluate expressions or look up variables
 *   - Produce any output
 */
public class Tokenizer {

    private final String source;
    private int pos;                            // index of next character to read
    private int line;                           // current 1-based line number

    // Indentation stack — each entry is the column width of one open block level.
    // Starts at [0] representing the top-level (no indentation).
    private final Deque<Integer> indentStack = new ArrayDeque<>();

    /**
     * @param source the complete ZARA source code as a single String
     */
    public Tokenizer(String source) {
        this.source = source;
        this.pos    = 0;
        this.line   = 1;
        this.indentStack.push(0);              // level 0 = top level, no indent
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Tokenizes the entire source string.
     *
     * @return ordered list of tokens, always ending with an EOF token
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < source.length()) {
            // At the start of every line, handle indentation BEFORE reading tokens.
            // measureIndent() looks ahead without consuming non-whitespace characters.
            // This must run at position 0 and after every newline is consumed.
            processIndentation(tokens);

            // Now read tokens on this line until we hit \n or end of source
            while (pos < source.length() && peek() != '\n') {

                skipWhitespace();               // skip spaces/tabs within a line

                if (pos >= source.length() || peek() == '\n') {
                    break;                      // hit end of line — stop inner loop
                }

                char c = peek();

                if (c == '"') {
                    tokens.add(readString());
                } else if (Character.isDigit(c)) {
                    tokens.add(readNumber());
                } else if (Character.isLetter(c) || c == '_') {
                    tokens.add(readIdentifierOrKeyword());
                } else {
                    tokens.add(readOperator());
                }
            }

            // Consume the newline itself and emit a NEWLINE token
            if (pos < source.length() && peek() == '\n') {
                tokens.add(new Token(TokenType.NEWLINE, "\n", line));
                advance();
                line++;
            }
        }

        // At EOF close any open indentation levels with DEDENT tokens
        while (indentStack.peek() > 0) {
            tokens.add(new Token(TokenType.DEDENT, "", line));
            indentStack.pop();
        }

        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    // ── Indentation handling ──────────────────────────────────────────────────

    /**
     * Measures the indentation of the current line and emits INDENT or DEDENT
     * tokens as needed. Called once at the start of every line.
     *
     * Blank lines (only whitespace / newline) are skipped — they do not
     * affect indentation and do not produce any tokens.
     *
     * Algorithm:
     *   1. Count leading spaces/tabs on the current line (do not advance pos
     *      past anything that is not whitespace).
     *   2. If the line is blank, return immediately — no indent change.
     *   3. Compare measured indent to the top of indentStack:
     *        greater  → push new level, emit INDENT
     *        less     → pop levels until stack top matches, emit DEDENT per pop
     *        equal    → do nothing
     */
    private void processIndentation(List<Token> tokens) {
        // Count leading whitespace without consuming it yet
        int indent = 0;
        int lookahead = pos;
        while (lookahead < source.length() &&
               (source.charAt(lookahead) == ' ' || source.charAt(lookahead) == '\t')) {
            indent++;
            lookahead++;
        }

        // If this is a blank line (all whitespace or empty), skip it entirely
        if (lookahead >= source.length() || source.charAt(lookahead) == '\n') {
            return;
        }

        // Actually consume the leading whitespace now
        while (pos < source.length() &&
               (peek() == ' ' || peek() == '\t')) {
            advance();
        }

        int currentLevel = indentStack.peek();

        if (indent > currentLevel) {
            // Indentation increased → new block opened
            indentStack.push(indent);
            tokens.add(new Token(TokenType.INDENT, "", line));

        } else if (indent < currentLevel) {
            // Indentation decreased → one or more blocks closed
            while (indentStack.peek() > indent) {
                indentStack.pop();
                tokens.add(new Token(TokenType.DEDENT, "", line));
            }
            // Guard: if the indent doesn't match any known level it is a syntax error
            if (indentStack.peek() != indent) {
                throw new RuntimeException(
                    "Line " + line + ": Indentation error — does not match any outer block level");
            }
        }
        // If indent == currentLevel: nothing to emit, normal line
    }

    // ── Private readers ───────────────────────────────────────────────────────

    /**
     * Reads a numeric literal (integer or decimal).
     * Examples in ZARA source: 10  3  85  3.14
     */
    private Token readNumber() {
        int start = pos;
        while (pos < source.length() && (Character.isDigit(peek()) || peek() == '.')) {
            advance();
        }
        String raw = source.substring(start, pos);
        return new Token(TokenType.NUMBER, raw, line);
    }

    /**
     * Reads a quoted string literal.
     * The opening " has already been peeked but NOT consumed.
     * The returned token's value does NOT include the surrounding quotes.
     *
     * Fix (review point 4): throws RuntimeException with line number if the
     * string is never closed before end of file.
     *
     * Example:  source text  "Hello from ZARA"
     *           token value   Hello from ZARA
     */
    private Token readString() {
        int openLine = line;
        advance();                              // consume the opening "
        int start = pos;

        while (pos < source.length() && peek() != '"') {
            if (peek() == '\n') {
                // strings do not span lines in ZARA
                throw new RuntimeException(
                    "Line " + openLine + ": Unterminated string literal");
            }
            advance();
        }

        if (pos >= source.length()) {
            // reached EOF without finding closing "
            throw new RuntimeException(
                "Line " + openLine + ": Unterminated string literal — missing closing '\"'");
        }

        String content = source.substring(start, pos);
        advance();                              // consume the closing "
        return new Token(TokenType.STRING, content, line);
    }

    /**
     * Reads an identifier or keyword.
     * Identifiers are sequences of letters, digits, and underscores.
     * After reading, the text is compared against the ZARA keyword list.
     * If it matches a keyword, the corresponding TokenType is used.
     * Otherwise the token type is IDENTIFIER.
     *
     * ZARA keywords: set  show  when  loop
     */
    private Token readIdentifierOrKeyword() {
        int start = pos;
        while (pos < source.length() &&
               (Character.isLetterOrDigit(peek()) || peek() == '_')) {
            advance();
        }
        String word = source.substring(start, pos);

        TokenType type = switch (word) {
            case "set"  -> TokenType.SET;
            case "show" -> TokenType.SHOW;
            case "when" -> TokenType.WHEN;
            case "loop" -> TokenType.LOOP;
            default     -> TokenType.IDENTIFIER;
        };

        return new Token(type, word, line);
    }

    /**
     * Reads one operator token.
     *
     * Special case — '=' vs '==':
     *   After consuming '=', peek at the next character.
     *   If it is also '=', consume it and emit EQUALS.
     *   Otherwise emit ASSIGN.
     *
     * Fix (review point 2): '(' and ')' now produce LPAREN and RPAREN
     * instead of throwing an exception.
     */
    private Token readOperator() {
        char c = advance();

        TokenType type = switch (c) {
            case '+' -> TokenType.PLUS;
            case '-' -> TokenType.MINUS;
            case '*' -> TokenType.STAR;
            case '/' -> TokenType.SLASH;
            case '>' -> TokenType.GREATER;
            case '<' -> TokenType.LESS;
            case ':' -> TokenType.COLON;
            case '(' -> TokenType.LPAREN;
            case ')' -> TokenType.RPAREN;
            case '=' -> {
                if (pos < source.length() && peek() == '=') {
                    advance();              // consume the second '='
                    yield TokenType.EQUALS;
                } else {
                    yield TokenType.ASSIGN;
                }
            }
            default -> throw new RuntimeException(
                "Line " + line + ": Unexpected character '" + c + "'");
        };

        // '==' needs a two-character value string; everything else is one char
        String value = (type == TokenType.EQUALS) ? "==" : String.valueOf(c);
        return new Token(type, value, line);
    }

    // ── Navigation helpers ────────────────────────────────────────────────────

    /**
     * Advances pos past any space or tab characters on the current line.
     * Does NOT skip newlines — newlines are consumed explicitly in tokenize().
     */
    private void skipWhitespace() {
        while (pos < source.length() && (peek() == ' ' || peek() == '\t')) {
            advance();
        }
    }

    /**
     * Returns the character at the current position without advancing pos.
     */
    private char peek() {
        return source.charAt(pos);
    }

    /**
     * Returns the character at the current position and advances pos by one.
     * This is the only place pos is incremented.
     */
    private char advance() {
        return source.charAt(pos++);
    }
}