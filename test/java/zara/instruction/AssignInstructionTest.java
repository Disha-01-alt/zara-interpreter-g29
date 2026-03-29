package zara.instruction;

import org.junit.jupiter.api.Test;
import zara.ast.Expression;
import zara.runtime.Environment;
import zara.instruction.AssignInstruction;

import static org.junit.jupiter.api.Assertions.*;

class AssignInstructionTest {

    @Test
    void shouldStoreValueInEnvironment() {
        // Arrange
        Environment env = new Environment();

        Expression expr = e -> 10.0;

        AssignInstruction assign = new AssignInstruction("x", expr);

        // Act
        assign.execute(env);

        // Assert
        assertEquals(10.0, env.get("x"));
    }

    @Test
    void shouldOverwriteExistingVariable() {
        // Arrange
        Environment env = new Environment();

        Expression expr1 = e -> 10.0;
        Expression expr2 = e -> 20.0;

        AssignInstruction a1 = new AssignInstruction("x", expr1);
        AssignInstruction a2 = new AssignInstruction("x", expr2);

        // Act
        a1.execute(env);
        a2.execute(env);

        // Assert
        assertEquals(20.0, env.get("x"));
    }

    @Test
    void shouldWorkWithDifferentVariableNames() {
        // Arrange
        Environment env = new Environment();

        Expression expr = e -> 50.0;

        AssignInstruction assign = new AssignInstruction("score", expr);

        // Act
        assign.execute(env);

        // Assert
        assertEquals(50.0, env.get("score"));
    }
}