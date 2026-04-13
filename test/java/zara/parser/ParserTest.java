
package zara.parser;

import zara.parser.Parser;
import zara.runtime.Environment;
import org.junit.jupiter.api.Test;
import zara.instruction.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    // ---------------------------------------------------------------------
    // Helper — tokenize + parse in one call
    // ---------------------------------------------------------------------

    private List<Instruction> parse(String source) {
        zara.lexer.Tokenizer tokenizer = new zara.lexer.Tokenizer(source);
        List<zara.lexer.Token> tokens = tokenizer.tokenize();
        Parser parser = new Parser(tokens);
        return parser.parse();
    }

    private Instruction parseOne(String source) {
        List<Instruction> instructions = parse(source);
        assertFalse(instructions.isEmpty(), "Expected at least one instruction");
        return instructions.get(0);
    }

    // ----------------------------------------------------------------
    // 1. Assignment tests
    // ---------------------------------------------------------------

    @Test
    void testAssignNumberLiteral() {
        Environment env = new Environment();
        parseOne("set x = 42").execute(env);
        assertEquals(42.0, env.get("x"));
    }

    @Test
    void testAssignStringLiteral() {
        Environment env = new Environment();
        parseOne("set name = \"Sitare\"").execute(env);
        assertEquals("Sitare", env.get("name"));
    }

    @Test
    void testAssignFromVariable() {
        Environment env = new Environment();
        List<Instruction> instructions = parse("set a = 10\nset b = a");
        instructions.forEach(i -> i.execute(env));
        assertEquals(10.0, env.get("b"));
    }

    @Test
    void testAssignOverwritesExistingVariable() {
        Environment env = new Environment();
        List<Instruction> instructions = parse("set x = 5\nset x = 99");
        instructions.forEach(i -> i.execute(env));
        assertEquals(99.0, env.get("x"));
    }

    @Test
    void testAssignProducesAssignInstruction() {
        Instruction instr = parseOne("set x = 10");
        assertInstanceOf(AssignInstruction.class, instr);
    }

    // -----------------------------------------------------------------------
    // 2. Print tests
    // -----------------------------------------------------------------------

    @Test
    void testShowProducesPrintInstruction() {
        Instruction instr = parseOne("show 42");
        assertInstanceOf(PrintInstruction.class, instr);
    }

    @Test
    void testShowStringLiteral() {
        Environment env = new Environment();
        assertDoesNotThrow(() -> parseOne("show \"hello\"").execute(env));
    }

    @Test
    void testShowVariable() {
        Environment env = new Environment();
        env.set("x", 7.0);
        assertDoesNotThrow(() -> parseOne("show x").execute(env));
    }

    // -----------------------------------------------------------------------
    // 3. Arithmetic & operator precedence
    // -----------------------------------------------------------------------

    @Test
    void testAddition() {
        Environment env = new Environment();
        parseOne("set r = 3 + 4").execute(env);
        assertEquals(7.0, env.get("r"));
    }

    @Test
    void testSubtraction() {
        Environment env = new Environment();
        parseOne("set r = 10 - 3").execute(env);
        assertEquals(7.0, env.get("r"));
    }

    @Test
    void testMultiplication() {
        Environment env = new Environment();
        parseOne("set r = 3 * 4").execute(env);
        assertEquals(12.0, env.get("r"));
    }

    @Test
    void testDivision() {
        Environment env = new Environment();
        parseOne("set r = 10 / 2").execute(env);
        assertEquals(5.0, env.get("r"));
    }

    @Test
    void testMultiplicationBindsTighterThanAddition() {
        Environment env = new Environment();
        parseOne("set r = 2 + 3 * 4").execute(env);
        assertEquals(14.0, env.get("r"));
    }

    @Test
    void testMultiplicationBindsTighterThanSubtraction() {
        Environment env = new Environment();
        parseOne("set r = 10 - 2 * 3").execute(env);
        assertEquals(4.0, env.get("r"));
    }

    @Test
    void testChainedAdditions() {
        Environment env = new Environment();
        parseOne("set r = 1 + 2 + 3").execute(env);
        assertEquals(6.0, env.get("r"));
    }

    @Test
    void testComplexExpression() {
        Environment env = new Environment();
        env.set("x", 10.0);
        env.set("y", 3.0);
        parseOne("set result = x + y * 2").execute(env);
        assertEquals(16.0, env.get("result"));
    }

    // -----------------------------------------------------------------------
    // 4. Comparison operators
    // -----------------------------------------------------------------------

    @Test
    void testGreaterThanTrue() {
        Environment env = new Environment();
        parse("when 5 > 3:\n    set flag = 1")
                .forEach(i -> i.execute(env));
        assertEquals(1.0, env.get("flag"));
    }

    @Test
    void testGreaterThanFalse() {
        Environment env = new Environment();
        parse("when 3 > 5:\n    set flag = 1")
                .forEach(i -> i.execute(env));
        assertThrows(RuntimeException.class, () -> env.get("flag"));
    }

    @Test
    void testLessThanTrue() {
        Environment env = new Environment();
        parse("when 2 < 10:\n    set flag = 1")
                .forEach(i -> i.execute(env));
        assertEquals(1.0, env.get("flag"));
    }

    // -----------------------------------------------------------------------
    // 5. Conditional (when) tests
    // -----------------------------------------------------------------------

    @Test
    void testWhenProducesIfInstruction() {
        Instruction instr = parseOne("when 1 > 0:\n    set x = 1");
        assertInstanceOf(IfInstruction.class, instr);
    }

    @Test
    void testWhenBodyExecutesWhenTrue() {
        Environment env = new Environment();
        parse("when 10 > 5:\n    set result = 99")
                .forEach(i -> i.execute(env));
        assertEquals(99.0, env.get("result"));
    }

    @Test
    void testWhenBodySkippedWhenFalse() {
        Environment env = new Environment();
        parse("when 1 > 5:\n    set result = 99")
                .forEach(i -> i.execute(env));
        assertThrows(RuntimeException.class, () -> env.get("result"));
    }

    @Test
    void testStatementAfterWhenAlwaysRuns() {
        Environment env = new Environment();
        parse("when 1 > 5:\n    set a = 1\nset b = 2")
                .forEach(i -> i.execute(env));
        assertThrows(RuntimeException.class, () -> env.get("a"));
        assertEquals(2.0, env.get("b"));
    }

    // -----------------------------------------------------------------------
    // 6. Loop tests
    // -----------------------------------------------------------------------

    @Test
    void testLoopProducesRepeatInstruction() {
        Instruction instr = parseOne("loop 3:\n    set x = 1");
        assertInstanceOf(RepeatInstruction.class, instr);
    }

    @Test
    void testLoopExecutesCorrectTimes() {
        Environment env = new Environment();
        env.set("count", 0.0);
        parse("loop 4:\n    set count = count + 1")
                .forEach(i -> i.execute(env));
        assertEquals(4.0, env.get("count"));
    }

    @Test
    void testLoopZeroTimes() {
        Environment env = new Environment();
        parse("loop 0:\n    set x = 1")
                .forEach(i -> i.execute(env));
        assertThrows(RuntimeException.class, () -> env.get("x"));
    }

    // -----------------------------------------------------------------------
    // 7. Nested blocks
    // -----------------------------------------------------------------------

    @Test
    void testWhenInsideLoop() {
        Environment env = new Environment();
        env.set("count", 0.0);
        parse("loop 3:\n    when 1 > 0:\n        set count = count + 1")
                .forEach(i -> i.execute(env));
        assertEquals(3.0, env.get("count"));
    }

    // -----------------------------------------------------------------------
    // 8. Error cases
    // -----------------------------------------------------------------------

    @Test
    void testMissingColonAfterWhen() {
        assertThrows(RuntimeException.class,
                () -> parse("when x > 0\n    set y = 1"));
    }

    @Test
    void testMissingAssignOperator() {
        assertThrows(RuntimeException.class,
                () -> parse("set x 10"));
    }

    @Test
    void testEmptyBlockAfterWhen() {
        assertThrows(RuntimeException.class,
                () -> parse("when 1 > 0:\nset x = 1"));
    }

    @Test
    void testUndefinedVariableIsRuntimeNotParseError() {
        assertDoesNotThrow(() -> {
            List<Instruction> instructions = parse("set x = undefined_var");
            assertThrows(RuntimeException.class,
                    () -> instructions.forEach(i -> i.execute(new Environment())));
        });
    }

    @Test
    void testMultipleTopLevelStatements() {
        List<Instruction> instructions = parse("set x = 1\nset y = 2\nset z = 3");
        assertEquals(3, instructions.size());
    }

    @Test
    void testBlankLinesBetweenStatements() {
        List<Instruction> instructions = parse("set x = 1\n\nset y = 2");
        assertEquals(2, instructions.size());
    }
}

