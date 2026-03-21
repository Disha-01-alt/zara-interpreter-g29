package lexer;

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
        // TODO: Walk through source character by character.
        // When you recognise a complete token, add it to a list.
        // At the end, add a token of type EOF.
        return tokens;
    }
}
