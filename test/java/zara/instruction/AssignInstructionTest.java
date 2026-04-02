package zara.instruction;

import org.junit.jupiter.api.Test;
import zara.ast.*;
import zara.instruction.AssignInstruction;
import zara.runtime.Environment;

import static org.junit.jupiter.api.Assertions.*;

class AssignInstructionTest {

    @Test
    void shouldStoreValueInEnvironment() {
        Environment env = new Environment();
        Expression expr = new NumberNode(10.0);

        AssignInstruction assign = new AssignInstruction("x", expr);
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

        Expression expr = new BinaryOpNode(
                new VariableNode("a"), "+", new NumberNode(5)
        );

        AssignInstruction assign = new AssignInstruction("result", expr);
        assign.execute(env);

        assertEquals(15.0, env.get("result"));
    }
}