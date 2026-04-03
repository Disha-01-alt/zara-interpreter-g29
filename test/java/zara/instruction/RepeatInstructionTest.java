package zara.instruction;

import org.junit.jupiter.api.Test;
import zara.ast.*;
import zara.runtime.Environment;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepeatInstructionTest {

    @Test
    void shouldExecuteBodyCorrectNumberOfTimes() {
        Environment env = new Environment();
        env.set("counter", 0.0);
        List<Instruction> body = Arrays.asList(
                new AssignInstruction("counter",
                        new BinaryOpNode(new VariableNode("counter"), "+", new NumberNode(1)))
        );
        RepeatInstruction repeat = new RepeatInstruction(5, body);
        repeat.execute(env);
        assertEquals(5.0, env.get("counter"));
    }

    @Test
    void shouldNotExecuteBodyWhenCountIsZero() {
        Environment env = new Environment();
        env.set("counter", 0.0);
        List<Instruction> body = Arrays.asList(
                new AssignInstruction("counter",
                        new BinaryOpNode(new VariableNode("counter"), "+", new NumberNode(1)))
        );
        RepeatInstruction repeat = new RepeatInstruction(0, body);
        repeat.execute(env);
        assertEquals(0.0, env.get("counter"));
    }

    @Test
    void shouldIncrementVariableInLoop() {
        Environment env = new Environment();
        env.set("i", 1.0);
        List<Instruction> body = Arrays.asList(
                new AssignInstruction("i",
                        new BinaryOpNode(new VariableNode("i"), "+", new NumberNode(1)))
        );
        RepeatInstruction repeat = new RepeatInstruction(4, body);
        repeat.execute(env);
        assertEquals(5.0, env.get("i"));
    }

    @Test
    void shouldThrowOnNegativeCount() {
        List<Instruction> body = Arrays.asList(new AssignInstruction("x", new NumberNode(1)));
        assertThrows(IllegalArgumentException.class, () ->
                new RepeatInstruction(-1, body));
    }

    @Test
    void shouldThrowOnNullBody() {
        assertThrows(IllegalArgumentException.class, () ->
                new RepeatInstruction(3, null));
    }

    @Test
    void shouldThrowOnEmptyBody() {
        assertThrows(IllegalArgumentException.class, () ->
                new RepeatInstruction(3, Arrays.asList()));
    }

    @Test
    void shouldExecuteSingleIteration() {
        Environment env = new Environment();
        env.set("x", 0.0);
        List<Instruction> body = Arrays.asList(
                new AssignInstruction("x", new NumberNode(99))
        );
        RepeatInstruction repeat = new RepeatInstruction(1, body);
        repeat.execute(env);
        assertEquals(99.0, env.get("x"));
    }

    @Test
    void shouldHandleMultipleBodyInstructions() {
        Environment env = new Environment();
        env.set("a", 0.0);
        env.set("b", 0.0);
        List<Instruction> body = Arrays.asList(
                new AssignInstruction("a", new BinaryOpNode(new VariableNode("a"), "+", new NumberNode(1))),
                new AssignInstruction("b", new BinaryOpNode(new VariableNode("b"), "+", new NumberNode(2)))
        );
        RepeatInstruction repeat = new RepeatInstruction(3, body);
        repeat.execute(env);
        assertEquals(3.0, env.get("a"));
        assertEquals(6.0, env.get("b"));
    }

    @Test
    void shouldReturnCorrectToString() {
        List<Instruction> body = Arrays.asList(new AssignInstruction("x", new NumberNode(1)));
        RepeatInstruction repeat = new RepeatInstruction(5, body);
        assertTrue(repeat.toString().contains("count=5"));
    }
}