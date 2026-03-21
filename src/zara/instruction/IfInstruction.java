package zara.instruction;

import zara.ast.Expression;
import zara.runtime.Environment;

public class IfInstruction implements Instruction {
    private final Expression condition;
    private final Instruction body;

    public IfInstruction(Expression condition, Instruction body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void execute(Environment env) {
        // TODO: Execute If Block
    }
}
