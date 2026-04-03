package zara.instruction;

import zara.runtime.Environment;
import java.util.List;

public class RepeatInstruction implements Instruction {

    private final int count;
    private final List<Instruction> body;

    public RepeatInstruction(int count, List<Instruction> body) {
        if (count < 0) {
            throw new IllegalArgumentException("Loop count cannot be negative: " + count);
        }
        if (body == null || body.isEmpty()) {
            throw new IllegalArgumentException("Body cannot be null or empty for loop statement");
        }
        this.count = count;
        this.body = body;
    }

    @Override
    public void execute(Environment env) {
        try {
            for (int i = 0; i < count; i++) {
                for (Instruction instr : body) {
                    instr.execute(env);
                }
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Error in loop statement at iteration: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "RepeatInstruction{count=" + count + ", body=" + body.size() + " statements}";
    }
}