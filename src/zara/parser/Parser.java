package zara.parser;

import zara.ast.*;
import zara.instruction.*;
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
        while (!isAtEnd()) {

            if (match(TokenType.NEWLINE)) continue;   // to Skip any stray newlines between instructions
            instructions.add(parseInstruction());
        }
        return instructions;
    }

    private Instruction parseInstruction() {
        if (match(TokenType.SET)) return assignment();
        if (match(TokenType.SHOW)) return print();
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

    private Instruction print() {
        Expression value = parseComparison();
        return new PrintInstruction(value);
    }

    private Instruction ifStatement() {
        Expression condition = parseComparison();
        consume(TokenType.COLON, "Expect ':' after condition.");
        // Consume a newline if it exists after the colon
        match(TokenType.NEWLINE);

        List<Instruction> body = parseBlock();
        return new IfInstruction(condition, body);
    }

    private Instruction loopStatement() {
        Expression count = parseComparison();
        consume(TokenType.COLON, "Expect ':' after loop count.");
        match(TokenType.NEWLINE);

        List<Instruction> body = parseBlock();
        return new RepeatInstruction(count, body);
    }

    private List<Instruction> parseBlock() {
        List<Instruction> body = new ArrayList<>();
        // Simple ZARA block: continue until we hit a new keyword or EOF
        // Note: Real ZARA uses indentation, but this works for basic scripts
        while (!isAtEnd() && !isKeyword(peek().getType())) {
            if (match(TokenType.NEWLINE)) continue;
            body.add(parseInstruction());
        }
        return body;
    }

    // --- Expression Precedence ---

    private Expression parseComparison() {
        Expression expr = parseExpression();   // 1. First, get the left side (handles +, -, *, / via parseExpression)
        while (match(TokenType.GREATER_THAN, TokenType.LESS_THAN, TokenType.EQUAL_EQUAL)) {  // 2. Check for any of the three comparison operators
            Token operator = previous();
            String op = operator.getValue();
            Expression right = parseExpression();  // 3. Get the right side
            expr = new BinaryOpNode(expr, op, right);  // 4. Wrap them together in a BinaryOpNode
        }
        return expr;
    }

    private Expression parseExpression() {
        Expression expr = parseTerm();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            String op = previous().getValue();
            Expression right = parseTerm();
            expr = new BinaryOpNode(expr, op, right);
        }
        return expr;
    }


    private Expression parseTerm() {
        Expression expr = parsePrimary();
        while (match(TokenType.MULTIPLY, TokenType.DIVIDE)) {
            String op = previous().getValue();
            Expression right = parsePrimary();
            expr = new BinaryOpNode(expr, op, right);
        }
        return expr;
    }

    private Expression parsePrimary() {
        if (match(TokenType.NUMBER)) {
            return new NumberNode(Double.parseDouble(previous().getValue()));
        }
        if (match(TokenType.STRING)) {
            return new StringNode(previous().getValue());
        }
        if (match(TokenType.IDENTIFIER)) {
            return new VariableNode(previous().getValue());
        }

        throw new RuntimeException("Expect expression at line " + peek().getLine());
    }

    // --- Helpers ---

    private boolean isKeyword(TokenType type) {
        return type == TokenType.SET || type == TokenType.SHOW ||
                type == TokenType.WHEN || type == TokenType.LOOP;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw new RuntimeException(message + " at line " + peek().getLine());
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return current >= tokens.size() || peek().getType() == TokenType.EOF;
    }

    private Token peek() { return tokens.get(current); }
    private Token previous() { return tokens.get(current - 1); }
}