package zara.instruction;

import zara.ast.Expression;
import zara.runtime.Environment;

public class PrintInstruction implements Instruction
{
    private final Expression expression;

    public PrintInstruction(Expression expression)
    {
        this.expression = expression;
    }

    @Override
    public void execute(Environment env)
    {

        Object result = expression.evaluate(env);
        if (result instanceof Double)
        {
            double d = (Double) result;
            if (d == Math.floor(d))
            {
                System.out.println((int) d);
            }
            else
            {
                System.out.println(d);
            }
        }
        else
        {
            System.out.println(result);
        }
    }

    @Override
    public String toString() {
        return "PrintInstruction";
    }
}
