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


    /*
     * Parses:  show <expression> <NEWLINE>
     * Produces: PrintInstruction
     */
    private Instruction parsePrint() {
        expect(TokenType.SHOW);

        Expression expr = parseExpression();

        consumeNewlineOrEOF();

        return new PrintInstruction(expr);
    }


    /*
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


    /*
     * Parses:
     *   loop <NUMBER> :
     *       <indented block>
     * The loop count is a literal integer — not an expression.
     * Produces: RepeatInstruction
     */

    private Instruction parseLoop() {
        Token loopToken = expect(TokenType.LOOP);

        Token countToken = expect(TokenType.NUMBER);
        int count;
        try {
            count = (int) Double.parseDouble(countToken.getValue());
        } catch (NumberFormatException e) {
            throw new ParseException(
                    "Line " + countToken.getLine() + ": loop count must be an integer, got '"
                            + countToken.getValue() + "'.",
                    countToken.getLine()
            );
        }

        expect(TokenType.COLON);
        expect(TokenType.NEWLINE);

        List<Instruction> body = parseBlock(loopToken.getLine());

        return new RepeatInstruction(count, body);
    }


    // Block parser (indented body for when / loop)
    private List<Instruction> parseBlock(int headerLine) {
        List<Instruction> body = new ArrayList<>();

        // A block must contain at least one statement.
        if (current().getType() == TokenType.EOF || !isIndented(current())) {
            throw new ParseException(
                    "Line " + headerLine + ": expected an indented block after ':'.",
                    headerLine
            );
        }
        while (current().getType() != TokenType.EOF && isIndented(current())) {
            // Skip blank lines inside a block
            if (current().getType() == TokenType.NEWLINE) {
                consume();
                continue;
            }
            body.add(parseStatement());
        }
        return body;
    }

    // Determines whether a token sits on an indented line.
    private boolean isIndented(Token token) {
        int lineIndex = token.getLine() - 1; // Token.line is 1-based
        if (lineIndex < 0 || lineIndex >= sourceLines.length) {
            return false;
        }
        String line = sourceLines[lineIndex];
        return !line.isEmpty() && (line.charAt(0) == ' ' || line.charAt(0) == '\t');
    }


    // .....................Expression parsing — three-level precedence chain...........................

    // Level 1 — lowest precedence: +, -, >, <

    private Expression parseExpression() {
        Expression left = parseTerm();

        while (true) {
            TokenType type = current().getType();
            if (type == TokenType.PLUS || type == TokenType.MINUS
                    || type == TokenType.GREATER || type == TokenType.LESS) {
                String operator = consume().getValue();
                Expression right = parseTerm();
                left = new BinaryOpNode(operator, left, right);
            } else {
                break;
            }
        }

        return left;
    }

    // Level 2 — higher precedence: *, /

    private Expression parseTerm() {
        Expression left = parsePrimary();

        while (true) {
            TokenType type = current().getType();
            if (type == TokenType.STAR || type == TokenType.SLASH) {
                String operator = consume().getValue();
                Expression right = parsePrimary();
                left = new BinaryOpNode(operator, left, right);
            } else {
                break;
            }
        }

        return left;
    }




}