package main.java.zara.ast;
import main.java.zara.runtime.Environment;

public interface Expression {
    Object evaluate(Environment env);
}