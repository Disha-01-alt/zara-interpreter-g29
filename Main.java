public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Main <path_to_zara_file>");
            System.exit(1);
        }

        // TODO: Read source code from the file and pass to the Interpreter
        String sourceCode = ""; 
        
        runtime.Interpreter interpreter = new runtime.Interpreter();
        interpreter.run(sourceCode);
    }
}
