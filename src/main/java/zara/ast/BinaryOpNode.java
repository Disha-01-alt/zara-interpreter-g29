package zara.ast;

import zara.runtime.Environment;

/**
 * An expression node representing a binary operation: left OP right.
 *
 * Supported operators:
 *   Arithmetic:  +  -  *  /      (operands must both be Double)
 *   Comparison:  >  <            (operands must both be Double, returns Boolean)
 *   Equality:    ==              (works for both Double and String)
 *   String concatenation:  +     (if either side is a String)
 *
 * Division by zero throws a RuntimeException.
 */
public final class BinaryOpNode implements Expression {

    private final Expression left;
    private final String operator;
    private final Expression right;

    public BinaryOpNode(Expression left, String operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public Object evaluate(Environment env) {
        Object leftVal = left.evaluate(env);
        Object rightVal = right.evaluate(env);

        // ── Both operands are numbers ────────────────────────────────────────
        if (leftVal instanceof Double && rightVal instanceof Double) {
            double l = (Double) leftVal;
            double r = (Double) rightVal;

            switch (operator) {
                case "+":  return l + r;
                case "-":  return l - r;
                case "*":  return l * r;
                case "/":
                    if (r == 0) {
                        throw new RuntimeException("Division by zero");
                    }
                    return l / r;
                case ">":  return l > r;
                case "<":  return l < r;
                case "==": return l == r;
                default:
                    throw new RuntimeException("Unknown operator: " + operator);
            }
        }

        // ── String concatenation with + ──────────────────────────────────────
        if (operator.equals("+")) {
            // At least one side is a String → concatenate
            if (leftVal instanceof String || rightVal instanceof String) {
                String l = stringify(leftVal);
                String r = stringify(rightVal);
                return l + r;
            }
        }

        // ── String equality with == ──────────────────────────────────────────
        if (operator.equals("==")) {
            if (leftVal instanceof String && rightVal instanceof String) {
                return leftVal.equals(rightVal);
            }
            // Mismatched types are never equal
            return false;
        }

        throw new RuntimeException(
                "Invalid operands for operator '" + operator
                        + "': " + typeName(leftVal) + " and " + typeName(rightVal));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Converts a value to its display string.
     * Whole-number Doubles print without the ".0" suffix.
     */
    private String stringify(Object value) {
        if (value instanceof Double) {
            double d = (Double) value;
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf((int) d);
            }
            return String.valueOf(d);
        }
        return String.valueOf(value);
    }

    @Override
    public String toString() {
        return "BinaryOpNode(" + operator + ")";
    }

    private String typeName(Object value) {
        if (value instanceof Double)  return "Number";
        if (value instanceof String)  return "String";
        if (value instanceof Boolean) return "Boolean";
        return value == null ? "null" : value.getClass().getSimpleName();
    }
}