package zara.instruction;

import org.junit.jupiter.api.Test;
import zara.ast.*;
import zara.runtime.Environment;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class PrintInstructionTest {

    @Test
    void shouldPrintIntegerWithoutDecimal() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        Environment env = new Environment();
        PrintInstruction print = new PrintInstruction(new NumberNode(16));
        print.execute(env);

        assertEquals("16", output.toString().trim());
        System.setOut(System.out);
    }

    @Test
    void shouldPrintDecimalNumber() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        Environment env = new Environment();
        PrintInstruction print = new PrintInstruction(new NumberNode(3.14));
        print.execute(env);

        assertEquals("3.14", output.toString().trim());
        System.setOut(System.out);
    }

    @Test
    void shouldPrintString() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        Environment env = new Environment();
        PrintInstruction print = new PrintInstruction(new StringNode("Hello from ZARA"));
        print.execute(env);

        assertEquals("Hello from ZARA", output.toString().trim());
        System.setOut(System.out);
    }

    @Test
    void shouldPrintVariableValue() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        Environment env = new Environment();
        env.set("name", "Sitare");
        PrintInstruction print = new PrintInstruction(new VariableNode("name"));
        print.execute(env);

        assertEquals("Sitare", output.toString().trim());
        System.setOut(System.out);
    }
}