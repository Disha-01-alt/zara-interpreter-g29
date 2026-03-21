package zara;

public class BinaryOpNode implements Expression {
    private final Expression left;
    private final String operator;
    private final Expression right;

    public BinaryOpNode(Expression left, String operator, Expression right) {
        // TODO: Store all three fields
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public Object evaluate(Environment env) {
        // TODO:
        // 1. Evaluate left and right
        // 2. Cast both to Double for arithmetic
        // 3. Switch on operator:
        //    "+" -> left + right (Double)
        //    "-" -> left - right (Double)
        //    "*" -> left * right (Double)
        //    "/" -> left / right (Double)
        //    ">" -> left > right (Boolean)
        //    "<" -> left < right (Boolean)
        //    "==" -> left.equals(right) (Boolean)
        return null;
    }
}
