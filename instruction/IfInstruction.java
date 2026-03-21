package instruction;

import ast.Expression;
import runtime.Environment;

public class IfInstruction implements Instruction {
    private final Expression condition;
    private final Instruction body; // Usually a BlockInstruction

    public IfInstruction(Expression condition, Instruction body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void execute(Environment env) {
        // TODO: Evaluate the condition. If true, execute the body.
    }
}
