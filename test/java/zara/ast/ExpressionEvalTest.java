package zara.ast;

import org.junit.jupiter.api.Test;
import zara.runtime.Environment;
import zara.ast.*;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionEvalTest {

    // -----------------------------------------------------------------------
    // 1. NumberNode
    // -----------------------------------------------------------------------

    @Test
    void testNumberNode() {
        NumberNode node = new NumberNode(42.0);
        Environment env = new Environment();

        assertEquals(42.0, node.evaluate(env));
    }

    // -----------------------------------------------------------------------
    // 2. StringNode
    // -----------------------------------------------------------------------

    @Test
    void testStringNode() {
        StringNode node = new StringNode("hello");
        Environment env = new Environment();

        assertEquals("hello", node.evaluate(env));
    }

    // -----------------------------------------------------------------------
    // 3. VariableNode
    // -----------------------------------------------------------------------

    @Test
    void testVariableNode() {
        Environment env = new Environment();
        env.set("x", 10.0);

        VariableNode node = new VariableNode("x");

        assertEquals(10.0, node.evaluate(env));
    }

    @Test
    void testVariableNodeUndefined() {
        Environment env = new Environment();
        VariableNode node = new VariableNode("y");

        assertThrows(RuntimeException.class, () -> node.evaluate(env));
    }

    // -----------------------------------------------------------------------
    // 4. BinaryOpNode - Arithmetic
    // -----------------------------------------------------------------------

    @Test
    void testAddition() {
        Expression expr = new BinaryOpNode("+",
                new NumberNode(3),
                new NumberNode(4));

        assertEquals(7.0, expr.evaluate(new Environment()));
    }

    @Test
    void testSubtraction() {
        Expression expr = new BinaryOpNode("-",
                new NumberNode(10),
                new NumberNode(3));

        assertEquals(7.0, expr.evaluate(new Environment()));
    }

    @Test
    void testMultiplication() {
        Expression expr = new BinaryOpNode("*",
                new NumberNode(3),
                new NumberNode(4));

        assertEquals(12.0, expr.evaluate(new Environment()));
    }

    @Test
    void testDivision() {
        Expression expr = new BinaryOpNode("/",
                new NumberNode(10),
                new NumberNode(2));

        assertEquals(5.0, expr.evaluate(new Environment()));
    }

    @Test
    void testDivisionByZero() {
        Expression expr = new BinaryOpNode("/",
                new NumberNode(10),
                new NumberNode(0));

        assertThrows(RuntimeException.class, () -> expr.evaluate(new Environment()));
    }

    // -----------------------------------------------------------------------
    // 5. BinaryOpNode - String concatenation
    // -----------------------------------------------------------------------

    @Test
    void testStringConcatenation() {
        Expression expr = new BinaryOpNode("+",
                new StringNode("Hello "),
                new StringNode("World"));

        assertEquals("Hello World", expr.evaluate(new Environment()));
    }

    @Test
    void testMixedConcatenation() {
        Expression expr = new BinaryOpNode("+",
                new StringNode("Value: "),
                new NumberNode(10));

        assertEquals("Value: 10", expr.evaluate(new Environment()));
    }

    // -----------------------------------------------------------------------
    // 6. BinaryOpNode - Comparison
    // -----------------------------------------------------------------------

    @Test
    void testGreaterThanTrue() {
        Expression expr = new BinaryOpNode(">",
                new NumberNode(5),
                new NumberNode(3));

        assertEquals(true, expr.evaluate(new Environment()));
    }

    @Test
    void testGreaterThanFalse() {
        Expression expr = new BinaryOpNode(">",
                new NumberNode(2),
                new NumberNode(3));

        assertEquals(false, expr.evaluate(new Environment()));
    }

    @Test
    void testLessThan() {
        Expression expr = new BinaryOpNode("<",
                new NumberNode(2),
                new NumberNode(10));

        assertEquals(true, expr.evaluate(new Environment()));
    }

    @Test
    void testEqualityNumbers() {
        Expression expr = new BinaryOpNode("==",
                new NumberNode(5),
                new NumberNode(5));

        assertEquals(true, expr.evaluate(new Environment()));
    }

    @Test
    void testEqualityStrings() {
        Expression expr = new BinaryOpNode("==",
                new StringNode("hi"),
                new StringNode("hi"));

        assertEquals(true, expr.evaluate(new Environment()));
    }

    // -----------------------------------------------------------------------
    // 7. BinaryOpNode - Type errors
    // -----------------------------------------------------------------------

    @Test
    void testInvalidOperationOnString() {
        Expression expr = new BinaryOpNode("-",
                new StringNode("hello"),
                new NumberNode(5));

        assertThrows(RuntimeException.class, () -> expr.evaluate(new Environment()));
    }

    @Test
    void testInvalidOperator() {
        Expression expr = new BinaryOpNode("%",
                new NumberNode(5),
                new NumberNode(2));

        assertThrows(RuntimeException.class, () -> expr.evaluate(new Environment()));
    }
}

