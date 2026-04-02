package zara.ast;
import zara.runtime.Environment;

public final class VariableNode implements Expression {

    private final String name;

    public VariableNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Object evaluate(Environment env) {
        return env.get(name);
    }

    @Override
    public String toString() {
        return "VariableNode(" + name + ")";
    }
}