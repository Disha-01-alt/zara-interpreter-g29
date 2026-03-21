package instruction;

import runtime.Environment;

public interface Instruction {
    // Execute this instruction, reading and writing variables via the Environment.
    void execute(Environment env);
}
