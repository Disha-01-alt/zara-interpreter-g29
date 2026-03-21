package zara;

public class NumberNode implements Expression {
    private final double value;

    public NumberNode(double value) {
        // TODO: Store value
        this.value = value;
    }

    @Override
    public Object evaluate(Environment env) {
        // TODO: Return stored number
        return null;
    }
}
