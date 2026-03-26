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
    private Instruction parseInstruction(){
        if(match(TokenType.SET)) return assignments();
        if(match(TokenType.SHOW)) return print();
        if (match(TokenType.WHEN)) return ifStatement();
        if (match(TokenType.LOOP)) return loopStatement();

        throw new RuntimeException("Unexpected token at line " + peek().getLine() + ": " + peek().getValue());
    }


    private Instruction assignment() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name after 'set'.");
        consume(TokenType.ASSIGN, "Expect '=' after variable name.");
        Expression value = parseComparison();
        return new AssignInstruction(name.getValue(), value);
    }


}
