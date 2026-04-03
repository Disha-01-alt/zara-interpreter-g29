package zara.instruction;

import org.junit.jupiter.api.Test;
import zara.runtime.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests — run complete ZARA programs through
 * the full pipeline (Tokenizer → Parser → Executor) and verify stdout.
 *
 * These correspond to the four required sample programs from the project spec.
 */
class InterpreterIntegrationTest {

    // ── Helper ────────────────────────────────────────────────────────────────

    /**
     * Runs sourceCode through the Interpreter and returns whatever was
     * printed to stdout, with trailing whitespace trimmed.
     */
    private String runAndCapture(String sourceCode) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new Interpreter().run(sourceCode);
        } finally {
            System.setOut(original);
        }
        return out.toString().trim();
    }

    // ── Program 1: Arithmetic and variables ───────────────────────────────────

    @Test
    void program1_arithmeticAndVariables() {
        String source =
                "set x = 10\n" +
                "set y = 3\n" +
                "set result = x + y * 2\n" +
                "show result\n";

        assertEquals("16", runAndCapture(source));
    }

    // ── Program 2: String output ──────────────────────────────────────────────

    @Test
    void program2_stringOutput() {
        String source =
                "set name = \"Sitare\"\n" +
                "show name\n" +
                "show \"Hello from ZARA\"\n";

        String expected = "Sitare\nHello from ZARA";
        assertEquals(expected, runAndCapture(source));
    }

    // ── Program 3: Conditional ────────────────────────────────────────────────

    @Test
    void program3_conditional() {
        String source =
                "set score = 85\n" +
                "when score > 50:\n" +
                "    show \"Pass\"\n";

        assertEquals("Pass", runAndCapture(source));
    }

    // ── Program 4: Loop ───────────────────────────────────────────────────────

    @Test
    void program4_loop() {
        String source =
                "set i = 1\n" +
                "loop 4:\n" +
                "    show i\n" +
                "    set i = i + 1\n";

        String expected = "1\n2\n3\n4";
        assertEquals(expected, runAndCapture(source));
    }

    // ── Additional edge-case tests ────────────────────────────────────────────

    @Test
    void conditionFalse_bodySkipped() {
        String source =
                "set score = 30\n" +
                "when score > 50:\n" +
                "    show \"Pass\"\n" +
                "show \"Done\"\n";

        assertEquals("Done", runAndCapture(source));
    }

    @Test
    void nestedWhenInsideLoop() {
        String source =
                "set i = 1\n" +
                "loop 3:\n" +
                "    when i > 1:\n" +
                "        show i\n" +
                "    set i = i + 1\n";

        String expected = "2\n3";
        assertEquals(expected, runAndCapture(source));
    }

    @Test
    void loopZeroTimes_bodyNotExecuted() {
        String source =
                "loop 0:\n" +
                "    show \"never\"\n" +
                "show \"done\"\n";

        assertEquals("done", runAndCapture(source));
    }
}
