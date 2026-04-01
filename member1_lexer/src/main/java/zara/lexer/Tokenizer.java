package zara.lexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage 1 of the ZARA pipeline.
 *
 * Reads a raw source String character by character and produces a
 * flat List<Token>.  The last token in the list is always EOF.
 *
 * Responsibilities (this class only):
 *   - Recognise every token type defined in TokenType
 *   - Track line numbers accurately
 *   - Strip quote characters from string literals
 *   - Distinguish '=' (ASSIGN) from '==' (EQUALS)
 *   - Skip spaces and tabs (but NOT newlines — they are significant)
 *
 * This class does NOT:
 *   - Understand grammar or instruction structure (that is the Parser's job)
 *   - Evaluate expressions or look up variables
 *   - Produce any output
 */
public class Tokenizer {

    private final String source;
    private int pos;    // index of the character we are about to read
    private int line;   // current 1-based line number

    /**
     * @param source the complete ZARA source code as a single String
     */
    public Tokenizer(String source) {
        this.source = source;
        this.pos    = 0;
        this.line   = 1;
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

            skipWhitespace();               // skip spaces/tabs, leave pos on next real char

            if (pos >= source.length()) {
                break;
            }

            char c = peek();

            if (c == '\n') {
                tokens.add(new Token(TokenType.NEWLINE, "\n", line));
                advance();
                line++;

            } else if (c == '"') {
                tokens.add(readString());

            } else if (Character.isDigit(c)) {
                tokens.add(readNumber());

            } else if (Character.isLetter(c) || c == '_') {
                tokens.add(readIdentifierOrKeyword());

            } else {
                tokens.add(readOperator());
            }
        }

        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
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
     * Example:  source text  "Hello from ZARA"
     *           token value   Hello from ZARA
     */
    private Token readString() {
        advance();                          // consume the opening "
        int start = pos;
        while (pos < source.length() && peek() != '"') {
            advance();
        }
        String content = source.substring(start, pos);
        if (pos < source.length()) {
            advance();                      // consume the closing "
        }
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
     * Reads a single operator character (or '==' as a two-character operator).
     *
     * The critical case is '=':
     *   If the next character is also '=', emit EQUALS ("==").
     *   Otherwise emit ASSIGN ("=").
     *
     * All other operators are exactly one character.
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
            case '=' -> {
                // peek ahead: '==' is EQUALS, lone '=' is ASSIGN
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

        // For '==', value is "=="; for everything else, value is the single char
        String value = (type == TokenType.EQUALS) ? "==" : String.valueOf(c);
        return new Token(type, value, line);
    }

    // ── Navigation helpers ────────────────────────────────────────────────────

    /**
     * Advances pos past any space or tab characters.
     * Does NOT skip newlines — newlines are significant tokens in ZARA.
     */
    private void skipWhitespace() {
        while (pos < source.length() && (peek() == ' ' || peek() == '\t')) {
            advance();
        }
    }

    /**
     * Returns the character at the current position without advancing pos.
     * Call this to look ahead before deciding what to read.
     */
    private char peek() {
        return source.charAt(pos);
    }

    /**
     * Returns the character ONE position ahead of current without advancing.
     * Used only for '==' detection inside readOperator().
     * Returns '\0' if pos+1 is out of bounds.
     */
    private char peekNext() {
        if (pos + 1 >= source.length()) return '\0';
        return source.charAt(pos + 1);
    }

    /**
     * Returns the character at the current position and advances pos by one.
     * This is the only method that moves pos forward.
     */
    private char advance() {
        return source.charAt(pos++);
    }
}
