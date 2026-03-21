package instruction;

import ast.Expression;
import runtime.Environment;

public class AssignInstruction implements Instruction {
    private final String variableName;
    private final Expression expression;

    public AssignInstruction(String variableName, Expression expression) {
        this.variableName = variableName;
        this.expression = expression;
    }

    @Override
    public void execute(Environment env) {
        // TODO: Evaluate the expression, then store the result in the Environment.
    }
}
