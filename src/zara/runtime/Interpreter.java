package zara.runtime;

import zara.instruction.Instruction;
import zara.lexer.Token;
import zara.lexer.Tokenizer;
import zara.parser.Parser;

import java.util.List;

public class Interpreter {
    public void run(String sourceCode) {
        Tokenizer tokenizer = new Tokenizer(sourceCode);
        List<Token> tokens = tokenizer.tokenize();

        Parser parser = new Parser(tokens);
        List<Instruction> instructions = parser.parse();

        Environment env = new Environment();
        for (Instruction instruction : instructions) {
            instruction.execute(env);
        }
    }
}
