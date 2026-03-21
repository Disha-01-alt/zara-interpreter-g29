package instruction;

import ast.Expression;
import runtime.Environment;

public class RepeatInstruction implements Instruction {
    private final Expression countExpression;
    private final Instruction body; // Usually a BlockInstruction

    public RepeatInstruction(Expression countExpression, Instruction body) {
        this.countExpression = countExpression;
        this.body = body;
    }

    @Override
    public void execute(Environment env) {
        // TODO: Evaluate countExpression, then execute the body count times.
    }
}
