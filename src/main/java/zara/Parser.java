package zara;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current;

    public Parser(List<Token> tokens) {
        // TODO: Store tokens, current = 0
        this.tokens = tokens;
        this.current = 0;
    }

    public List<Instruction> parse() {
        List<Instruction> instructions = new ArrayList<>();
        // TODO: Loop through tokens until EOF
        // - Skip NEWLINE tokens
        // - If current token is SET -> parseAssign()
        // - If current token is SHOW -> parsePrint()
        // - If current token is WHEN -> parseWhen()
        // - If current token is LOOP -> parseLoop()
        // - Else -> throw error with line number
        return instructions;
    }

    // ---- Instruction Parsers ----

    // TODO: Instruction parseAssign()
    //   consume SET, consume IDENTIFIER (save name),
    //   consume ASSIGN, parse expression, return AssignInstruction

    // TODO: Instruction parsePrint()
    //   consume SHOW, parse expression, return PrintInstruction

    // TODO: Instruction parseWhen()
    //   consume WHEN, parse expression (condition),
    //   consume COLON, consume NEWLINE,
    //   parse indented body lines, return IfInstruction

    // TODO: Instruction parseLoop()
    //   consume LOOP, read number (count),
    //   consume COLON, consume NEWLINE,
    //   parse indented body lines, return RepeatInstruction

    // ---- Expression Parsers (OPERATOR PRECEDENCE) ----

    // TODO: Expression parseExpression()
    //   handles + and - and comparison operators
    //   calls parseTerm() for left side
    //   while next token is +, -, >, <, == :
    //     consume operator, parseTerm() for right,
    //     wrap in BinaryOpNode

    // TODO: Expression parseTerm()
    //   handles * and /
    //   calls parsePrimary() for left side
    //   while next token is * or / :
    //     consume operator, parsePrimary() for right,
    //     wrap in BinaryOpNode

    // TODO: Expression parsePrimary()
    //   if NUMBER -> return new NumberNode
    //   if STRING -> return new StringNode
    //   if IDENTIFIER -> return new VariableNode
    //   if LPAREN -> parseExpression(), consume RPAREN
    //   else -> throw error

    // ---- Helper Methods ----

    // TODO: Token peek() - return current token without advancing
    // TODO: Token advance() - return current token and move to next
    // TODO: Token consume(TokenType) - advance if match, else error
    // TODO: boolean check(TokenType) - does current token match?
}
