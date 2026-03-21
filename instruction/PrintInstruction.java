package instruction;

import ast.Expression;
import runtime.Environment;

public class PrintInstruction implements Instruction {
    private final Expression expression;

    public PrintInstruction(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void execute(Environment env) {
        // TODO: Evaluate the expression and print the result.
    }
}
