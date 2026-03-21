package zara.instruction;

import zara.runtime.Environment;
import java.util.List;

public class BlockInstruction implements Instruction {
    private final List<Instruction> instructions;

    public BlockInstruction(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    @Override
    public void execute(Environment env) {
        for (Instruction instruction : instructions) {
            instruction.execute(env);
        }
    }
}
