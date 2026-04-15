package zara.runtime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import zara.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterEdgeCasesTest {

    private Interpreter interpreter;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        interpreter = new Interpreter();
        System.setOut(new PrintStream(outContent));
    }

    private String getOutput() {
        return outContent.toString().replace("\r\n", "\n").trim();
    }

    // ── 1. Case Sensitivity ──────────────────────────────────────────────────
    
    @Test
    void testCaseSensitivityKeywordVsVariable() {
        // "SET" is not the "set" keyword, so it is an identifier.
        // This program should trigger a ParseException because "SET" is an invalid statement start.
        String program = "SET x = 10\n";
        assertThrows(ParseException.class, () -> interpreter.run(program), 
                "Should fail because SET is treated as identifier, not a valid statement start");
    }

    @Test
    void testVariableCaseSensitivity() {
        // Variables 'myVar' and 'myvar' are distinct
        String program = "" +
                "set myVar = 10\n" +
                "set myvar = 20\n" +
                "show myVar\n" +
                "show myvar\n";
        interpreter.run(program);
        assertEquals("10\n20", getOutput());
    }

    // ── 2. Grammar Edge Cases (Missing tokens) ───────────────────────────────

    @Test
    void testMissingColonInWhen() {
        String program = "" +
                "set x = 10\n" +
                "when x > 5\n" +  // missing colon here
                "    show x\n";
        assertThrows(ParseException.class, () -> interpreter.run(program));
    }

    @Test
    void testMissingEqualSignInAssignment() {
        String program = "set x 10\n";
        assertThrows(ParseException.class, () -> interpreter.run(program));
    }

    @Test
    void testMissingExpressionInPrint() {
        String program = "show \n";
        assertThrows(ParseException.class, () -> interpreter.run(program));
    }

    // ── 3. Deep Nesting ──────────────────────────────────────────────────────

    @Test
    void testExtremelyDeepParentheses() {
        // Nested parenthesis 10 layers deep
        String program = "" +
                "set val = ((((((((((5))))))))))\n" +
                "show val\n";
        interpreter.run(program);
        assertEquals("5", getOutput());
    }

    @Test
    void testDeeplyNestedBlocks() {
        String program = "" +
                "set x = 1\n" +
                "when x == 1:\n" +
                "    loop 2:\n" +
                "        when x < 10:\n" +
                "            loop 1:\n" +
                "                show x\n" +
                "                set x = x + 1\n";
        interpreter.run(program);
        assertEquals("1\n2", getOutput());
    }

    // ── 4. Edge Case Arithmetic ──────────────────────────────────────────────

    @Test
    void testComplexMathPrecedenceAndSign() {
        // ZARA doesn't have unary minus natively, but we can do 0 - X
        String program = "" +
                "set a = 1 + 2 * 3 - 8 / 4\n" + // 1 + 6 - 2 = 5
                "set b = 0 - a * 2\n" +         // 0 - 10 = -10
                "show a\n" +
                "show b\n";
        interpreter.run(program);
        assertEquals("5\n-10", getOutput());
    }

    @Test
    void testStringEqualityWithVariables() {
        String program = "" +
                "set s1 = \"hello\"\n" +
                "set s2 = \"hello\"\n" +
                "set s3 = \"world\"\n" +
                "when s1 == s2:\n" +
                "    show \"Match1\"\n" +
                "when s1 == s3:\n" +
                "    show \"Match2\"\n";
        interpreter.run(program);
        assertEquals("Match1", getOutput()); // Match2 should not print
    }

    // ── 5. Unconventional Whitespace ─────────────────────────────────────────

    @Test
    void testWhitespaceOnlyProgram() {
        String program = "   \n  \t  \n\n";
        interpreter.run(program);
        assertEquals("", getOutput()); // Should not fail, just do nothing
    }

    @Test
    void testProgramEndingWithoutNewline() {
        String program = "set result = 42\nshow result"; // No trailing \n
        interpreter.run(program);
        assertEquals("42", getOutput());
    }
}
