package zara.ast;

import zara.runtime.Environment;

public interface Expression {
    Object evaluate(Environment env);
}