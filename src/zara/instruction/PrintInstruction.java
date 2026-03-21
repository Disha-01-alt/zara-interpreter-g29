package zara.instruction;

import zara.ast.Expression;
import zara.runtime.Environment;

public class PrintInstruction implements Instruction {
    private final Expression expression;

    public PrintInstruction(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void execute(Environment env) {
        // TODO: Execute Print
    }
}
