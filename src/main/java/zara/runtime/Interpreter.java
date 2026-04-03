package zara.runtime;

import zara.instruction.Instruction;
import zara.lexer.Token;
import zara.lexer.Tokenizer;
import zara.parser.Parser;

import java.util.List;

/**
 * Pipeline orchestrator — the entry point for running a ZARA program.
 *
 * Connects all three stages:
 *   1. Tokenizer:  source String  → List<Token>
 *   2. Parser:     List<Token>    → List<Instruction>
 *   3. Executor:   walks the instruction list, calling execute(env) on each
 *
 * Error handling:
 *   A single top-level try/catch prints any parse or runtime error
 *   with a clear message and exits cleanly. No other class needs catch blocks.
 */
public class Interpreter {

    /**
     * Runs a complete ZARA program.
     *
     * @param sourceCode the full text of a .zara source file
     */
    public void run(String sourceCode) {
        try {
            // Stage 1 — Tokenize
            Tokenizer tokenizer = new Tokenizer(sourceCode);
            List<Token> tokens = tokenizer.tokenize();

            // Stage 2 — Parse
            Parser parser = new Parser(tokens);
            List<Instruction> instructions = parser.parse();

            // Stage 3 — Execute
            Environment env = new Environment();
            for (Instruction instr : instructions) {
                instr.execute(env);
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
