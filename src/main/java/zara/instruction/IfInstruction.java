package zara.instruction;

import zara.ast.Expression;
import zara.runtime.Environment;

import java.util.List;

public class IfInstruction implements Instruction {

    private final Expression condition;
    private final List<Instruction> body;

    public IfInstruction(Expression condition, List<Instruction> body) {
        if (condition == null) {
            throw new IllegalArgumentException("Condition cannot be null for when statement");
        }
        if (body == null || body.isEmpty()) {
            throw new IllegalArgumentException("Body cannot be null or empty for when statement");
        }
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void execute(Environment env) {
        try {
            Object result = condition.evaluate(env);
            if (!(result instanceof Boolean)) {
                throw new RuntimeException("Condition must evaluate to a boolean, got: " + result.getClass().getSimpleName());
            }
            if ((Boolean) result) {
                for (Instruction instr : body) {
                    instr.execute(env);
                }
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Error in when statement: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "IfInstruction{condition=" + condition + ", body=" + body.size() + " statements}";
    }
}