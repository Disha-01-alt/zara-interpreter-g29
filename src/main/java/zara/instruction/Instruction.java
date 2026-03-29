package zara.instruction;

import zara.runtime.Environment;

public interface Instruction
{
    void execute(Environment env);
}
