package zara.instruction;

import org.junit.jupiter.api.Test;
import zara.ast.*;
import zara.runtime.Environment;

import static org.junit.jupiter.api.Assertions.*;

class AssignInstructionTest {

    @Test
    void shouldStoreValueInEnvironment() {
        Environment env = new Environment();
        AssignInstruction assign = new AssignInstruction("x", new NumberNode(10.0));
        assign.execute(env);
        assertEquals(10.0, env.get("x"));
    }

    @Test
    void shouldOverwriteExistingVariable() {
        Environment env = new Environment();
        env.set("x", 5.0);
        AssignInstruction assign = new AssignInstruction("x", new NumberNode(20.0));
        assign.execute(env);
        assertEquals(20.0, env.get("x"));
    }

    @Test
    void shouldEvaluateExpressionBeforeStoring() {
        Environment env = new Environment();
        env.set("a", 10.0);
        Expression expr = new BinaryOpNode(new VariableNode("a"), "+", new NumberNode(5));
        AssignInstruction assign = new AssignInstruction("result", expr);
        assign.execute(env);
        assertEquals(15.0, env.get("result"));
    }

    @Test
    void shouldStoreStringValue() {
        Environment env = new Environment();
        AssignInstruction assign = new AssignInstruction("name", new StringNode("ZARA"));
        assign.execute(env);
        assertEquals("ZARA", env.get("name"));
    }

    @Test
    void shouldThrowOnNullVariableName() {
        assertThrows(IllegalArgumentException.class, () ->
                new AssignInstruction(null, new NumberNode(10)));
    }

    @Test
    void shouldThrowOnEmptyVariableName() {
        assertThrows(IllegalArgumentException.class, () ->
                new AssignInstruction("", new NumberNode(10)));
    }

    @Test
    void shouldThrowOnNullExpression() {
        assertThrows(IllegalArgumentException.class, () ->
                new AssignInstruction("x", null));
    }

    @Test
    void shouldThrowWhenExpressionUsesUndefinedVariable() {
        Environment env = new Environment();
        AssignInstruction assign = new AssignInstruction("y", new VariableNode("undefined"));
        assertThrows(RuntimeException.class, () -> assign.execute(env));
    }

    @Test
    void shouldHandleComplexExpression() {
        Environment env = new Environment();
        env.set("x", 10.0);
        env.set("y", 3.0);
        // result = x + y * 2 = 10 + 6 = 16
        Expression expr = new BinaryOpNode(
                new VariableNode("x"), "+",
                new BinaryOpNode(new VariableNode("y"), "*", new NumberNode(2))
        );
        AssignInstruction assign = new AssignInstruction("result", expr);
        assign.execute(env);
        assertEquals(16.0, env.get("result"));
    }

    @Test
    void shouldReturnCorrectToString() {
        AssignInstruction assign = new AssignInstruction("x", new NumberNode(5));
        assertTrue(assign.toString().contains("x"));
    }
}