package zara.instruction;

import org.junit.jupiter.api.Test;
import zara.ast.*;
import zara.runtime.Environment;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class PrintInstructionTest {

    private String captureOutput(Runnable action) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(output));
        action.run();
        System.setOut(original);
        return output.toString().replace("\r", "").trim();
    }

    @Test
    void shouldPrintIntegerWithoutDecimal() {
        Environment env = new Environment();
        PrintInstruction print = new PrintInstruction(new NumberNode(16));
        assertEquals("16", captureOutput(() -> print.execute(env)));
    }

    @Test
    void shouldPrintDecimalNumber() {
        Environment env = new Environment();
        PrintInstruction print = new PrintInstruction(new NumberNode(3.14));
        assertEquals("3.14", captureOutput(() -> print.execute(env)));
    }

    @Test
    void shouldPrintString() {
        Environment env = new Environment();
        PrintInstruction print = new PrintInstruction(new StringNode("Hello from ZARA"));
        assertEquals("Hello from ZARA", captureOutput(() -> print.execute(env)));
    }

    @Test
    void shouldPrintVariableValue() {
        Environment env = new Environment();
        env.set("name", "Sitare");
        PrintInstruction print = new PrintInstruction(new VariableNode("name"));
        assertEquals("Sitare", captureOutput(() -> print.execute(env)));
    }

    @Test
    void shouldPrintZeroAsInteger() {
        Environment env = new Environment();
        PrintInstruction print = new PrintInstruction(new NumberNode(0));
        assertEquals("0", captureOutput(() -> print.execute(env)));
    }

    @Test
    void shouldPrintNegativeInteger() {
        Environment env = new Environment();
        PrintInstruction print = new PrintInstruction(new NumberNode(-5));
        assertEquals("-5", captureOutput(() -> print.execute(env)));
    }

    @Test
    void shouldPrintEmptyString() {
        Environment env = new Environment();
        PrintInstruction print = new PrintInstruction(new StringNode(""));
        assertEquals("", captureOutput(() -> print.execute(env)));
    }

    @Test
    void shouldThrowOnNullExpression() {
        assertThrows(IllegalArgumentException.class, () ->
                new PrintInstruction(null));
    }

    @Test
    void shouldThrowOnUndefinedVariable() {
        Environment env = new Environment();
        PrintInstruction print = new PrintInstruction(new VariableNode("unknown"));
        assertThrows(RuntimeException.class, () -> print.execute(env));
    }

    @Test
    void shouldReturnCorrectToString() {
        PrintInstruction print = new PrintInstruction(new NumberNode(42));
        assertTrue(print.toString().contains("PrintInstruction"));
    }
}