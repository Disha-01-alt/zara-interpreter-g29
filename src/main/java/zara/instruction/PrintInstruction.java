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
            if (result instanceof Double) {
                double d = (Double) result;
                if (d == Math.floor(d)) {
                    System.out.println((int) d);
                } else {
                    System.out.println(d);
                }
            } else {
                System.out.println(result);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Error in show statement: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "PrintInstruction{expression=" + expression + "}";
    }
}