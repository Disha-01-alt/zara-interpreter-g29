package zara.instruction;

import zara.ast.Expression;
import zara.runtime.Environment;

public class PrintInstruction implements Instruction {

    private final Expression expression;

    public PrintInstruction(Expression expression) {
        if (expression == null) {
            throw new IllegalArgumentException("Expression cannot be null for print");
        }
        this.expression = expression;
    }

    @Override
    public void execute(Environment env) {
        try {
            Object result = expression.evaluate(env);
            if (result == null) {
                throw new RuntimeException("Cannot print null value");
            }
            System.out.println(format(result));
        } catch (RuntimeException e) {
            throw new RuntimeException("Error in show statement: " + e.getMessage());
        }
    }

    private String format(Object value) {
        if (value instanceof Double) {
            double d = (Double) value;
            if (d == Math.floor(d)) {
                return String.valueOf((int) d);
            }
            return String.valueOf(d);
        }
        return value.toString();
    }

    @Override
    public String toString() {
        return "PrintInstruction{expression=" + expression + "}";
    }
}