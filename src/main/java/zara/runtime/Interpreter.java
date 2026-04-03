package zara.runtime;

import zara.lexer.Tokenizer;
import zara.lexer.Token;
import zara.parser.Parser;
import zara.instruction.Instruction;

import java.util.List;

public class Interpreter {

    public void run(String sourceCode) {
        // Step 1: Tokenize
        Tokenizer tokenizer = new Tokenizer(sourceCode);
        List<Token> tokens = tokenizer.tokenize();

        // Step 2: Parse (source needed for indentation detection)
        Parser parser = new Parser(tokens);
        List<Instruction> instructions = parser.parse();

        // Step 3: Execute
        Environment env = new Environment();
        for (Instruction instr : instructions) {
            instr.execute(env);
        }
    }
}
