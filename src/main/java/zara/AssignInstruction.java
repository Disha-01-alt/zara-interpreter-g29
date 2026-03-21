package zara;

public class AssignInstruction implements Instruction {
    private final String variableName;
    private final Expression expression;

    public AssignInstruction(String variableName, Expression expression) {
        // TODO: Store both fields
        this.variableName = variableName;
        this.expression = expression;
    }

    @Override
    public void execute(Environment env) {
        // TODO: Evaluate expression, then env.set()
    }
}
