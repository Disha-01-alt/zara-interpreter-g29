package main.java.zara.parser;

import main.java.zara.lexer.Token;
import main.java.zara.lexer.TokenType;
import main.java.zara.ast.*;
import main.java.zara.instruction.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private final String[] sourceLines;
    private int pos;

    // Constructor

    public Parser(List<Token> tokens, String sourceCode) {
        this.tokens = tokens;
        this.sourceLines = sourceCode.split("\n", -1);
        this.pos = 0;
    }



    public List<Instruction> parse() {
        List<Instruction> instructions = new ArrayList<>();

        while (current().getType() != TokenType.EOF) {
            // Skip blank lines
            if (current().getType() == TokenType.NEWLINE) {
                consume();
                continue;
            }

            instructions.add(parseStatement());
        }

        return instructions;
    }

    // Statement dispatcher

    private Instruction parseStatement() {
        Token tok = current();

        switch (tok.getType()) {
            case SET:
                return parseAssignment();
            case SHOW:
                return parsePrint();
            case WHEN:
                return parseConditional();
            case LOOP:
                return parseLoop();

            default:
                throw new ParseException(
                        "Line " + tok.getLine() + ": unexpected token '" + tok.getValue()
                                + "'. Expected a statement (set / show / when / loop).",
                        tok.getLine()
                );
        }
    }


    /**
     * Parses:  set <identifier> = <expression> <NEWLINE>
     * Produces: AssignInstruction
     */
    private Instruction parseAssignment() {
        expect(TokenType.SET);

        Token nameToken = expect(TokenType.IDENTIFIER);
        String variableName = nameToken.getValue();

        expect(TokenType.ASSIGN);

        Expression expr = parseExpression();

        consumeNewlineOrEOF();

        return new AssignInstruction(variableName, expr);
    }


    /**
     * Parses:  show <expression> <NEWLINE>
     * Produces: PrintInstruction
     */
    private Instruction parsePrint() {
        expect(TokenType.SHOW);

        Expression expr = parseExpression();

        consumeNewlineOrEOF();

        return new PrintInstruction(expr);
    }


    /**
     * Parses:
     *   when <expression> :
     *       <indented block>
     * Produces: IfInstruction
     */
    private Instruction parseConditional() {
        Token whenToken = expect(TokenType.WHEN);

        Expression condition = parseExpression();

        expect(TokenType.COLON);
        expect(TokenType.NEWLINE);

        List<Instruction> body = parseBlock(whenToken.getLine());

        return new IfInstruction(condition, body);
    }



}