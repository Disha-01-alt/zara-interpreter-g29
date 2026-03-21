package parser;

import instruction.Instruction;
import lexer.Token;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Instruction> parse() {
        List<Instruction> instructions = new ArrayList<>();
        // Read through tokens and build the list of Instructions.
        return instructions;
    }

    // TODO: parseExpression(), parseTerm(), parsePrimary()
}
