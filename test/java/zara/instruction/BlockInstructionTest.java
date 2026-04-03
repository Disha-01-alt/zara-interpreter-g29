package zara.instruction;

import org.junit.jupiter.api.Test;
import zara.ast.*;
import zara.runtime.Environment;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class BlockInstructionTest {

    @Test
    void shouldExecuteAllInstructionsInOrder() {
        Environment env = new Environment();
        BlockInstruction block = new BlockInstruction(Arrays.asList(
                new AssignInstruction("a", new NumberNode(10)),
                new AssignInstruction("b", new NumberNode(20)),
                new AssignInstruction("c", new BinaryOpNode(new VariableNode("a"), "+", new VariableNode("b")))
        ));
        block.execute(env);
        assertEquals(10.0, env.get("a"));
        assertEquals(20.0, env.get("b"));
        assertEquals(30.0, env.get("c"));
    }

    @Test
    void shouldHandleEmptyBlock() {
        Environment env = new Environment();
        BlockInstruction block = new BlockInstruction(Arrays.asList());
        block.execute(env);
        assertTrue(true);
    }

    @Test
    void shouldShareEnvironmentAcrossInstructions() {
        Environment env = new Environment();
        BlockInstruction block = new BlockInstruction(Arrays.asList(
                new AssignInstruction("x", new NumberNode(5)),
                new AssignInstruction("x", new BinaryOpNode(new VariableNode("x"), "*", new NumberNode(3)))
        ));
        block.execute(env);
        assertEquals(15.0, env.get("x"));
    }

    @Test
    void shouldThrowOnNullInstructionList() {
        assertThrows(IllegalArgumentException.class, () ->
                new BlockInstruction(null));
    }

    @Test
    void shouldThrowWhenInstructionFails() {
        Environment env = new Environment();
        BlockInstruction block = new BlockInstruction(Arrays.asList(
                new AssignInstruction("x", new VariableNode("undefined"))
        ));
        assertThrows(RuntimeException.class, () -> block.execute(env));
    }

    @Test
    void shouldReturnCorrectToString() {
        BlockInstruction block = new BlockInstruction(Arrays.asList(
                new AssignInstruction("x", new NumberNode(1)),
                new AssignInstruction("y", new NumberNode(2))
        ));
        assertTrue(block.toString().contains("2 statements"));
    }
}