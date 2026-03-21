package zara;

import java.util.List;

public class RepeatInstruction implements Instruction {
    private final int count;
    private final List<Instruction> body;

    public RepeatInstruction(int count, List<Instruction> body) {
        // TODO: Store both fields
        this.count = count;
        this.body = body;
    }

    @Override
    public void execute(Environment env) {
        // TODO: Execute body instructions 'count' times
    }
}
