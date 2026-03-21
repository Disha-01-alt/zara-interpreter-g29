package zara.parser;

import zara.ast.Expression;
import zara.instruction.Instruction;
import zara.lexer.Token;

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
        // TODO: Parser implementation
        return instructions;
    }
}
