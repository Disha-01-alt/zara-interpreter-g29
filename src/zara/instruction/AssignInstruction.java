package zara.instruction;

import zara.ast.Expression;
import zara.runtime.Environment;

public class AssignInstruction implements Instruction {
    private final String variableName;
    private final Expression expression;

    public AssignInstruction(String variableName, Expression expression) {
        this.variableName = variableName;
        this.expression = expression;
    }

    @Override
    public void execute(Environment env) {
        // TODO: Execute Assignment
    }
}
