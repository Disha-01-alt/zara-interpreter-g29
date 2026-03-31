package main.java.zara.ast;
import main.java.zara.runtime.Environment;

public final class NumberNode implements Expression {

    private final double value;

    public NumberNode(double value) {
        this.value = value;
    }

    @Override
    public Object evaluate(Environment env) {
        return value;
    }

    @Override
    public String toString() {
        return "NumberNode(" + value + ")";
    }
}