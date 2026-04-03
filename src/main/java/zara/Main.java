package zara;

import zara.runtime.Interpreter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * CLI entry point for the ZARA interpreter.
 *
 * Usage:  java zara.Main <source-file.zara>
 *
 * Reads the source file into a String and passes it to Interpreter.run().
 */
public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java zara.Main <source-file.zara>");
            System.exit(1);
        }

        String filePath = args[0];
        try {
            String sourceCode = Files.readString(Path.of(filePath));
            new Interpreter().run(sourceCode);
        } catch (IOException e) {
            System.err.println("Error: Could not read file '" + filePath + "': " + e.getMessage());
            System.exit(1);
        }
    }
}
