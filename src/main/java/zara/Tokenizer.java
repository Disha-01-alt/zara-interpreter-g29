package zara;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    private final String source;
    private int pos;
    private int line;

    public Tokenizer(String source) {
        // TODO: Store source, init pos=0, line=1
        this.source = source;
        this.pos = 0;
        this.line = 1;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        // TODO: Walk through source char by char
        // - Skip spaces and tabs (NOT newlines)
        // - On '\n': add NEWLINE token, increment line
        // - On digit: read full number (including decimals)
        // - On '"': read string until closing '"'
        // - On letter: read identifier, check if keyword
        // - On operator: +, -, *, /, >, <, =, ==, :
        // At end: add EOF token
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    // TODO: Add helper methods like:
    // - char peek() / char advance()
    // - Token readNumber()
    // - Token readString()
    // - Token readIdentifierOrKeyword()
}
