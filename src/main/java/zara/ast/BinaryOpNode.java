package zara.ast;
import zara.runtime.Environment;

public final class BinaryOpNode implements Expression {

    private final Expression left;
    private final String operator;
    private final Expression right;

    public BinaryOpNode(String operator, Expression left, Expression right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Override
    public Object evaluate(Environment env) {
        Object leftVal  = left.evaluate(env);
        Object rightVal = right.evaluate(env);

        switch (operator) {
            // ── Arithmetic ─-
            case "+": {
                // String concatenation if either side is a String
                if (leftVal instanceof String || rightVal instanceof String) {
                    return stringify(leftVal) + stringify(rightVal);
                }
                return toDouble(leftVal, "+") + toDouble(rightVal, "+");
            }
            case "-":
                return toDouble(leftVal, "-") - toDouble(rightVal, "-");

            case "*":
                return toDouble(leftVal, "*") * toDouble(rightVal, "*");

            case "/": {
                double divisor = toDouble(rightVal, "/");
                if (divisor == 0.0) {
                    throw new RuntimeException("Division by zero");
                }
                return toDouble(leftVal, "/") / divisor;
            }

            // ── Comparison ──
            case ">":
                return toDouble(leftVal, ">") > toDouble(rightVal, ">");

            case "<":
                return toDouble(leftVal, "<") < toDouble(rightVal, "<");

            case "==":
                // for both Double and String equality
                return leftVal.equals(rightVal);

            default:
                throw new RuntimeException("Unknown operator: '" + operator + "'");
        }
    }

    // ── Helpers ──

     // Casts Object to double or throws RuntimeException with operator for context.
    private double toDouble(Object value, String op) {
        if (value instanceof Double) {
            return (Double) value;
        }
        throw new RuntimeException(
                "Operator '" + op + "' requires a number, but got: " +
                        (value == null ? "null" : "\"" + value + "\" (String)")
        );
    }

    // Converts value to string, omitting the decimal for whole numbers (e.g., 16.0 becomes "16").

    private String stringify(Object value) {
        if (value instanceof Double) {
            double d = (Double) value;
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf((long) d);
            }
            return String.valueOf(d);
        }
        return String.valueOf(value);
    }

    @Override
    public String toString() {
        return "BinaryOpNode(" + operator + ", " + left + ", " + right + ")";
    }
}