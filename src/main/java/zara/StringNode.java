package zara;

public class StringNode implements Expression {
    private final String value;

    public StringNode(String value) {
        // TODO: Store value
        this.value = value;
    }

    @Override
    public Object evaluate(Environment env) {
        // TODO: Return stored string
        return null;
    }
}
