package zara.instruction;

import zara.ast.Expression;
import zara.runtime.Environment;

public class AssignInstruction implements Instruction {

    private final String variableName;
    private final Expression expression;

    public AssignInstruction(String variableName, Expression expression) {
        if (variableName == null || variableName.isEmpty()) {
            throw new IllegalArgumentException("Variable name cannot be null or empty");
        }
        if (expression == null) {
            throw new IllegalArgumentException("Expression cannot be null for assignment");
        }
        this.variableName = variableName;
        this.expression = expression;
    }

    @Override
    public void execute(Environment env) {
        try {
            Object value = expression.evaluate(env);
            env.set(variableName, value);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error assigning to variable '" + variableName + "': " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "AssignInstruction{variable='" + variableName + "', expression=" + expression + "}";
    }
}