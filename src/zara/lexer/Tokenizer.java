package zara.lexer;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    private final String source;
    private int currentPosition = 0;

    public Tokenizer(String source) {
        this.source = source;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        // TODO: Tokenizer implementation
        return tokens;
    }
}
