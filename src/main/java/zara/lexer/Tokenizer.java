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
 * INDENT / DEDENT logic:
 *   ZARA uses indentation to define blocks, exactly like Python.
 *   After every NEWLINE the Tokenizer measures the indentation of the
 *   next non-blank line (number of leading spaces/tabs).
 *
 *   - If indentation INCREASES  → emit one INDENT token
 *   - If indentation DECREASES  → emit one or more DEDENT tokens
 *   - If indentation is the SAME → emit nothing extra
 *
 * Example token stream for:
 *   when score > 50:       → WHEN IDENTIFIER GREATER NUMBER COLON NEWLINE
 *       show "Pass"        → INDENT SHOW STRING NEWLINE
 *   show "Done"            → DEDENT SHOW STRING NEWLINE EOF
 */
public class Tokenizer {

    private final String source;
    private int pos;
    private int line;

    private final Deque<Integer> indentStack = new ArrayDeque<>();

    public Tokenizer(String source) {
        this.source = source;
        this.pos    = 0;
        this.line   = 1;
        this.indentStack.push(0);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < source.length()) {
            processIndentation(tokens);

            while (pos < source.length() && peek() != '\n') {
                skipWhitespace();

                if (pos >= source.length() || peek() == '\n') {
                    break;
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

            if (pos < source.length() && peek() == '\n') {
                tokens.add(new Token(TokenType.NEWLINE, "\n", line));
                advance();
                line++;
            }
        }

        while (indentStack.peek() > 0) {
            tokens.add(new Token(TokenType.DEDENT, "", line));
            indentStack.pop();
        }

        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    // ── Indentation handling ──────────────────────────────────────────────────

    private void processIndentation(List<Token> tokens) {
        int indent = 0;
        int lookahead = pos;
        while (lookahead < source.length() &&
                (source.charAt(lookahead) == ' ' || source.charAt(lookahead) == '\t')) {
            indent++;
            lookahead++;
        }

        if (lookahead >= source.length() || source.charAt(lookahead) == '\n') {
            return;
        }

        while (pos < source.length() &&
                (peek() == ' ' || peek() == '\t')) {
            advance();
        }

        int currentLevel = indentStack.peek();

        if (indent > currentLevel) {
            indentStack.push(indent);
            tokens.add(new Token(TokenType.INDENT, "", line));

        } else if (indent < currentLevel) {
            while (indentStack.peek() > indent) {
                indentStack.pop();
                tokens.add(new Token(TokenType.DEDENT, "", line));
            }
            if (indentStack.peek() != indent) {
                throw new RuntimeException(
                        "Line " + line + ": Indentation error — does not match any outer block level");
            }
        }
    }

    // ── Private readers ───────────────────────────────────────────────────────

    private Token readNumber() {
        int start = pos;
        while (pos < source.length() && (Character.isDigit(peek()) || peek() == '.')) {
            advance();
        }
        return new Token(TokenType.NUMBER, source.substring(start, pos), line);
    }

    private Token readString() {
        int openLine = line;
        advance();
        int start = pos;

        while (pos < source.length() && peek() != '"') {
            if (peek() == '\n') {
                throw new RuntimeException(
                        "Line " + openLine + ": Unterminated string literal");
            }
            advance();
        }

        if (pos >= source.length()) {
            throw new RuntimeException(
                    "Line " + openLine + ": Unterminated string literal — missing closing '\"'");
        }

        String content = source.substring(start, pos);
        advance();
        return new Token(TokenType.STRING, content, line);
    }

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

    private Token readOperator() {
        char c = advance();

        TokenType type = switch (c) {
            case '+' -> TokenType.PLUS;
            case '-' -> TokenType.MINUS;
            case '*' -> TokenType.STAR;
            case '/' -> TokenType.SLASH;
            case ':' -> TokenType.COLON;
            case '(' -> TokenType.LPAREN;
            case ')' -> TokenType.RPAREN;
            case '=' -> {
                if (pos < source.length() && peek() == '=') {
                    advance();
                    yield TokenType.EQUALS;
                } else {
                    yield TokenType.ASSIGN;
                }
            }
            case '>' -> {
                if (pos < source.length() && peek() == '=') {
                    advance();
                    yield TokenType.GREATER_EQUAL;
                } else {
                    yield TokenType.GREATER;
                }
            }
            case '<' -> {
                if (pos < source.length() && peek() == '=') {
                    advance();
                    yield TokenType.LESS_EQUAL;
                } else {
                    yield TokenType.LESS;
                }
            }
            case '!' -> {
                if (pos < source.length() && peek() == '=') {
                    advance();
                    yield TokenType.NOT_EQUAL;
                } else {
                    throw new RuntimeException(
                            "Line " + line + ": Unexpected character '!' — did you mean '!='?");
                }
            }
            default -> throw new RuntimeException(
                    "Line " + line + ": Unexpected character '" + c + "'");
        };

        String value = switch (type) {
            case EQUALS        -> "==";
            case GREATER_EQUAL -> ">=";
            case LESS_EQUAL    -> "<=";
            case NOT_EQUAL     -> "!=";
            default            -> String.valueOf(c);
        };

        return new Token(type, value, line);
    }

    // ── Navigation helpers ────────────────────────────────────────────────────

    private void skipWhitespace() {
        while (pos < source.length() && (peek() == ' ' || peek() == '\t')) {
            advance();
        }
    }

    private char peek() {
        return source.charAt(pos);
    }

    private char advance() {
        return source.charAt(pos++);
    }
}