package zara.instruction;

import zara.ast.Expression;
import zara.runtime.Environment;
import java.util.List;

public class RepeatInstruction implements Instruction {

    private final int count;
    private final List<Instruction> body;

    public RepeatInstruction(int count, List<Instruction> body) {
        this.count = count;
        this.body = body;
    }

    @Override
    public void execute(Environment env) {
        for (int i = 0; i < count; i++) {
            for (Instruction instr : body) {
                instr.execute(env);
            }
        }
    }

    @Override
    public String toString() {
        return "RepeatInstruction(" + count + ")";
    }
}