package zara;

public class VariableNode implements Expression {
    private final String name;

    public VariableNode(String name) {
        // TODO: Store name
        this.name = name;
    }

    @Override
    public Object evaluate(Environment env) {
        // TODO: return env.get(name)
        return null;
    }
}
