package zara.runtime;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {

    private String runAndCapture(String sourceCode) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(output));

        new Interpreter().run(sourceCode);

        System.setOut(original);
        return output.toString().replace("\r", "").trim();
    }

    @Test
    void shouldRunArithmeticProgram() {
        String code = "set x = 10\nset y = 3\nset result = x + y * 2\nshow result";
        assertEquals("16", runAndCapture(code));
    }

    @Test
    void shouldRunStringProgram() {
        String code = "set name = \"Sitare\"\nshow name\nshow \"Hello from ZARA\"";
        assertEquals("Sitare\nHello from ZARA", runAndCapture(code));
    }

    @Test
    void shouldRunConditionalProgram() {
        String code = "set score = 85\nwhen score > 50:\n    show \"Pass\"";
        assertEquals("Pass", runAndCapture(code));
    }

    @Test
    void shouldRunLoopProgram() {
        String code = "set i = 1\nloop 4:\n    show i\n    set i = i + 1";
        assertEquals("1\n2\n3\n4", runAndCapture(code));
    }
}