package zara.instruction;

import org.junit.jupiter.api.Test;
import zara.ast.*;
import zara.runtime.Environment;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IfInstructionTest {

    @Test
    void shouldExecuteBodyWhenConditionIsTrue() {
        Environment env = new Environment();
        env.set("score", 85.0);
        Expression condition = new BinaryOpNode(new VariableNode("score"), ">", new NumberNode(50));
        List<Instruction> body = Arrays.asList(new AssignInstruction("result", new NumberNode(1)));
        IfInstruction ifInstr = new IfInstruction(condition, body);
        ifInstr.execute(env);
        assertEquals(1.0, env.get("result"));
    }

    @Test
    void shouldSkipBodyWhenConditionIsFalse() {
        Environment env = new Environment();
        env.set("score", 30.0);
        Expression condition = new BinaryOpNode(new VariableNode("score"), ">", new NumberNode(50));
        List<Instruction> body = Arrays.asList(new AssignInstruction("result", new NumberNode(1)));
        IfInstruction ifInstr = new IfInstruction(condition, body);
        ifInstr.execute(env);
        assertThrows(RuntimeException.class, () -> env.get("result"));
    }

    @Test
    void shouldExecuteMultipleBodyInstructions() {
        Environment env = new Environment();
        env.set("x", 10.0);
        Expression condition = new BinaryOpNode(new VariableNode("x"), ">", new NumberNode(5));
        List<Instruction> body = Arrays.asList(
                new AssignInstruction("a", new NumberNode(100)),
                new AssignInstruction("b", new NumberNode(200))
        );
        IfInstruction ifInstr = new IfInstruction(condition, body);
        ifInstr.execute(env);
        assertEquals(100.0, env.get("a"));
        assertEquals(200.0, env.get("b"));
    }

    @Test
    void shouldThrowOnNullCondition() {
        List<Instruction> body = Arrays.asList(new AssignInstruction("x", new NumberNode(1)));
        assertThrows(IllegalArgumentException.class, () ->
                new IfInstruction(null, body));
    }

    @Test
    void shouldThrowOnNullBody() {
        Expression condition = new BinaryOpNode(new NumberNode(1), ">", new NumberNode(0));
        assertThrows(IllegalArgumentException.class, () ->
                new IfInstruction(condition, null));
    }

    @Test
    void shouldThrowOnEmptyBody() {
        Expression condition = new BinaryOpNode(new NumberNode(1), ">", new NumberNode(0));
        assertThrows(IllegalArgumentException.class, () ->
                new IfInstruction(condition, Arrays.asList()));
    }

    @Test
    void shouldHandleEqualityCondition() {
        Environment env = new Environment();
        env.set("x", 10.0);
        Expression condition = new BinaryOpNode(new VariableNode("x"), "==", new NumberNode(10));
        List<Instruction> body = Arrays.asList(new AssignInstruction("match", new NumberNode(1)));
        IfInstruction ifInstr = new IfInstruction(condition, body);
        ifInstr.execute(env);
        assertEquals(1.0, env.get("match"));
    }

    @Test
    void shouldHandleLessThanCondition() {
        Environment env = new Environment();
        env.set("x", 3.0);
        Expression condition = new BinaryOpNode(new VariableNode("x"), "<", new NumberNode(5));
        List<Instruction> body = Arrays.asList(new AssignInstruction("small", new NumberNode(1)));
        IfInstruction ifInstr = new IfInstruction(condition, body);
        ifInstr.execute(env);
        assertEquals(1.0, env.get("small"));
    }

    @Test
    void shouldReturnCorrectToString() {
        Expression condition = new BinaryOpNode(new NumberNode(1), ">", new NumberNode(0));
        List<Instruction> body = Arrays.asList(new AssignInstruction("x", new NumberNode(1)));
        IfInstruction ifInstr = new IfInstruction(condition, body);
        assertTrue(ifInstr.toString().contains("1 statements"));
    }
}