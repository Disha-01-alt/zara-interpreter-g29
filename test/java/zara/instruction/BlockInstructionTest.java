package zara.instruction;

import org.junit.jupiter.api.Test;
import zara.ast.*;
import zara.runtime.Environment;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BlockInstructionTest {

    @Test
    void shouldExecuteAllInstructionsInOrder() {
        Environment env = new Environment();

        List<Instruction> instructions = Arrays.asList(
                new AssignInstruction("x", new NumberNode(10)),
                new AssignInstruction("y", new NumberNode(20)),
                new AssignInstruction("z",
                        new BinaryOpNode(new VariableNode("x"), "+", new VariableNode("y")))
        );

        BlockInstruction block = new BlockInstruction(instructions);
        block.execute(env);

        assertEquals(10.0, env.get("x"));
        assertEquals(20.0, env.get("y"));
        assertEquals(30.0, env.get("z"));
    }

    @Test
    void shouldHandleEmptyBlock() {
        Environment env = new Environment();

        BlockInstruction block = new BlockInstruction(Arrays.asList());
        block.execute(env);

        // No exception should be thrown
        assertTrue(true);
    }

    @Test
    void shouldShareEnvironmentAcrossInstructions() {
        Environment env = new Environment();

        List<Instruction> instructions = Arrays.asList(
                new AssignInstruction("counter", new NumberNode(0)),
                new AssignInstruction("counter",
                        new BinaryOpNode(new VariableNode("counter"), "+", new NumberNode(5))),
                new AssignInstruction("counter",
                        new BinaryOpNode(new VariableNode("counter"), "+", new NumberNode(3)))
        );

        BlockInstruction block = new BlockInstruction(instructions);
        block.execute(env);

        assertEquals(8.0, env.get("counter"));
    }
}