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
}