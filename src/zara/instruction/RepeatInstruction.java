package zara.instruction;

import zara.ast.Expression;
import zara.runtime.Environment;

public class RepeatInstruction implements Instruction {
    private final Expression countExpression;
    private final Instruction body;

    public RepeatInstruction(Expression countExpression, Instruction body) {
        this.countExpression = countExpression;
        this.body = body;
    }

    @Override
    public void execute(Environment env) {
        // TODO: Execute Repeat Block
    }
}
