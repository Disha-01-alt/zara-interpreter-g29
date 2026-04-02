package zara;

import zara.runtime.Interpreter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java zara.Main <filename.zara>");
            return;
        }

        try {
            String code = Files.readString(Path.of(args[0])).strip();
            new Interpreter().run(code);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Runtime error: " + e.getMessage());
        }
    }
}