package zara.instruction;

import zara.runtime.Environment;
import java.util.List;

public class BlockInstruction implements Instruction {

    private final List<Instruction> instructions;

    public BlockInstruction(List<Instruction> instructions) {
        if (instructions == null) {
            throw new IllegalArgumentException("Instruction list cannot be null for block");
        }
        this.instructions = instructions;
    }

    @Override
    public void execute(Environment env) {
        try {
            for (Instruction instr : instructions) {
                instr.execute(env);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Error executing block: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "BlockInstruction{instructions=" + instructions.size() + " statements}";
    }
}