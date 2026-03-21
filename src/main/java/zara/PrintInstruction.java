package zara;

public class PrintInstruction implements Instruction {
    private final Expression expression;

    public PrintInstruction(Expression expression) {
        // TODO: Store expression
        this.expression = expression;
    }

    @Override
    public void execute(Environment env) {
        // TODO: Evaluate and print
        // Hint: if result is Double and has no decimal part,
        //       print as integer (e.g., 16.0 -> "16")
    }
}
