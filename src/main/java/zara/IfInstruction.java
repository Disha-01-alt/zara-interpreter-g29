package zara;

import java.util.List;

public class IfInstruction implements Instruction {
    private final Expression condition;
    private final List<Instruction> body;

    public IfInstruction(Expression condition, List<Instruction> body) {
        // TODO: Store both fields
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void execute(Environment env) {
        // TODO: Evaluate condition,
        //       if (Boolean) true -> execute all body instructions
    }
}
