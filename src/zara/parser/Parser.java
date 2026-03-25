package zara.parser;

import zara.ast.Expression;
import zara.instruction.Instruction;
import zara.lexer.Token;
import zara.lexer.TokenType;

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
        while(!isAtEnd()){
            if(match(TokenType.NEWLINE)) continue;
            instructions.add(parseInstruction());
        }
        return instructions;
    }


}
