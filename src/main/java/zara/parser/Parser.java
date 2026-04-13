package zara.parser;

import zara.lexer.*;
import zara.ast.*;
import zara.instruction.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage 2 of the ZARA pipeline.
 *
 * Reads the List<Token> produced by the Tokenizer and builds a
 * List<Instruction> — the executable program.
 *
 * Grammar (simplified):
 *   program     → statement* EOF
 *   statement   → assignment | print | conditional | loop
 *   assignment  → SET IDENTIFIER ASSIGN expression NEWLINE
 *   print       → SHOW expression NEWLINE
 *   conditional → WHEN expression COLON NEWLINE INDENT statement+ DEDENT
 *   loop        → LOOP NUMBER COLON NEWLINE INDENT statement+ DEDENT
 *
 * Expression precedence (lowest → highest):
 *   1. parseExpression : +  -  >  <  ==
 *   2. parseTerm       : *  /
 *   3. parsePrimary    : NUMBER | STRING | IDENTIFIER | ( expression )
 */

public class Parser {

    private final List<Token> tokens;
    private int pos;

    /**
     * @param tokens the token list produced by Tokenizer.tokenize()
     */
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    // ── Public API ──────────────────────────────────────────

    /**
     * Parses the full token stream into a list of executable instructions.
     *
     * @return the program as an ordered list of instructions
     */
    public List<Instruction> parse() {
        List<Instruction> instructions = new ArrayList<>();

        while (current().getType() != TokenType.EOF) {
            // Skip blank / empty lines
            if (current().getType() == TokenType.NEWLINE) {
                consume();
                continue;
            }
            instructions.add(parseStatement());
        }

        return instructions;
    }

    // ── Statement dispatcher ──────────────────────────────────────────────────

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
                        "Line " + tok.getLine() + ": unexpected token '"
                                + tok.getValue()
                                + "'. Expected a statement (set / show / when / loop).",
                        tok.getLine()
                );
        }
    }

    // ── Statement parsers ──────────────────────────────────────────────────

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
     *   when <expression> : NEWLINE
     *       INDENT <block> DEDENT
     * Produces: IfInstruction
     */
    private Instruction parseConditional() {
        expect(TokenType.WHEN);

        Expression condition = parseExpression();

        expect(TokenType.COLON);
        expect(TokenType.NEWLINE);

        List<Instruction> body = parseBlock();

        return new IfInstruction(condition, body);
    }

    /**
     * Parses:
     *   loop <NUMBER> : NEWLINE
     *       INDENT <block> DEDENT
     * The loop count is a literal integer — not an expression.
     * Produces: RepeatInstruction
     */
    private Instruction parseLoop() {
        expect(TokenType.LOOP);

        Token countToken = expect(TokenType.NUMBER);
        int count;
        try {
            count = (int) Double.parseDouble(countToken.getValue());
        } catch (NumberFormatException e) {
            throw new ParseException(
                    "Line " + countToken.getLine()
                            + ": loop count must be an integer, got '"
                            + countToken.getValue() + "'.",
                    countToken.getLine()
            );
        }

        expect(TokenType.COLON);
        expect(TokenType.NEWLINE);

        List<Instruction> body = parseBlock();

        return new RepeatInstruction(count, body);
    }

    // ── Block parser (INDENT … statements … DEDENT) ──────────────────────────

    /**
     * Parses an indented block opened by INDENT and closed by DEDENT.
     * The block must contain at least one statement.
     */
    private List<Instruction> parseBlock() {
        expect(TokenType.INDENT);

        List<Instruction> body = new ArrayList<>();

        while (current().getType() != TokenType.DEDENT
                && current().getType() != TokenType.EOF) {
            // Skip blank lines inside a block
            if (current().getType() == TokenType.NEWLINE) {
                consume();
                continue;
            }
            body.add(parseStatement());
        }

        if (current().getType() == TokenType.DEDENT) {
            consume();  // consume the DEDENT
        }

        if (body.isEmpty()) {
            throw new ParseException(
                    "Empty block — expected at least one statement after ':'.",
                    current().getLine()
            );
        }

        return body;
    }

    // ── Expression parsing — three-level precedence chain ─────────────────────

    /**
     * Level 1 — lowest precedence: +  -  >  <  ==
     */
    private Expression parseExpression() {
        Expression left = parseTerm();

        while (true) {
            TokenType type = current().getType();
            if (type == TokenType.PLUS || type == TokenType.MINUS
                    || type == TokenType.GREATER || type == TokenType.LESS
                    || type == TokenType.EQUALS) {
                String operator = consume().getValue();
                Expression right = parseTerm();
                left = new BinaryOpNode(left, operator, right);
            } else {
                break;
            }
        }

        return left;
    }

    /**
     * Level 2 — higher precedence: *  /
     */
    private Expression parseTerm() {
        Expression left = parsePrimary();

        while (true) {
            TokenType type = current().getType();
            if (type == TokenType.STAR || type == TokenType.SLASH) {
                String operator = consume().getValue();
                Expression right = parsePrimary();
                left = new BinaryOpNode(left, operator, right);
            } else {
                break;
            }
        }

        return left;
    }

    /**
     * Level 3 — highest precedence / base case.
     *   NUMBER     → NumberNode
     *   STRING     → StringNode
     *   IDENTIFIER → VariableNode
     *   LPAREN     → ( expression )
     */
    private Expression parsePrimary() {
        Token tok = current();

        switch (tok.getType()) {

            case NUMBER: {
                consume();
                double value;
                try {
                    value = Double.parseDouble(tok.getValue());
                } catch (NumberFormatException e) {
                    throw new ParseException(
                            "Line " + tok.getLine()
                                    + ": invalid number literal '"
                                    + tok.getValue() + "'.",
                            tok.getLine()
                    );
                }
                return new NumberNode(value);
            }

            case STRING: {
                consume();
                return new StringNode(tok.getValue());
            }

            case IDENTIFIER: {
                consume();
                return new VariableNode(tok.getValue());
            }

            case LPAREN: {
                consume();  // consume '('
                Expression expr = parseExpression();
                expect(TokenType.RPAREN);
                return expr;
            }

            default:
                throw new ParseException(
                        "Line " + tok.getLine()
                                + ": expected a value (number, string, or variable) "
                                + "but found '" + tok.getValue()
                                + "' (" + tok.getType() + ").",
                        tok.getLine()
                );
        }
    }

    // ── Helper methods ────────────────────────────────────────────────────────

    /** Returns the current token without advancing pos. */
    private Token current() {
        return tokens.get(pos);
    }

    /** Returns the current token and advances pos by one. */
    private Token consume() {
        return tokens.get(pos++);
    }

    /**
     * Consumes the current token if it matches the expected type.
     *
     * @throws ParseException with line number if the match fails
     */
    private Token expect(TokenType expected) {
        Token tok = current();
        if (tok.getType() != expected) {
            throw new ParseException(
                    "Line " + tok.getLine() + ": expected " + expected
                            + " but found '" + tok.getValue()
                            + "' (" + tok.getType() + ").",
                    tok.getLine()
            );
        }
        return consume();
    }

    /**
     * Consumes a NEWLINE or accepts EOF at the end of a statement.
     */
    private void consumeNewlineOrEOF() {
        TokenType type = current().getType();
        if (type == TokenType.NEWLINE) {
            consume();
        } else if (type == TokenType.EOF || type == TokenType.DEDENT) {
            // Last statement in source or block may have no trailing newline
        } else {
            Token tok = current();
            throw new ParseException(
                    "Line " + tok.getLine()
                            + ": expected end of line after statement, "
                            + "but found '" + tok.getValue()
                            + "' (" + tok.getType() + ").",
                    tok.getLine()
            );
        }
    }
}