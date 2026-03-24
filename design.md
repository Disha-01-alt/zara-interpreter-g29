# ZARA Interpreter — System Design Document

> **Language:** ZARA (Zero-ceremony Arithmetic and Reasoning Assembler)  
> **Course:** Advanced OOP in Java — Sitare University  
> **Document Type:** Engineering Design & Architecture

---

## Table of Contents

1. [Philosophy & Design Goals](#1-philosophy--design-goals)
2. [High-Level Architecture](#2-high-level-architecture)
3. [Project Directory Structure](#3-project-directory-structure)
4. [The Pipeline: How Execution Flows](#4-the-pipeline-how-execution-flows)
5. [Module-by-Module Design](#5-module-by-module-design)
   - 5.1 [Token Layer](#51-token-layer)
   - 5.2 [Expression Tree (AST)](#52-expression-tree-ast)
   - 5.3 [Environment (Variable Store)](#53-environment-variable-store)
   - 5.4 [Instruction Layer](#54-instruction-layer)
   - 5.5 [Tokenizer](#55-tokenizer)
   - 5.6 [Parser](#56-parser)
   - 5.7 [Evaluator / Interpreter Entry Point](#57-evaluator--interpreter-entry-point)
6. [Design Principles Applied](#6-design-principles-applied)
7. [Key Design Decisions & Why](#7-key-design-decisions--why)
8. [Error Handling Strategy](#8-error-handling-strategy)
9. [Extension Points](#9-extension-points)
10. [What NOT To Do](#10-what-not-to-do)

---

## 1. Philosophy & Design Goals

Before writing a single line of code, the team must agree on what kind of system this is. A scripting engine interpreter is not a data-processing app — it is a **pipeline of transformations**, where each stage produces a well-defined output that the next stage consumes. The design must reflect that.

### Core Engineering Goals

| Goal | What it means in practice |
|------|---------------------------|
| **High Cohesion** | Every class has one clear job. `Tokenizer` only tokenizes. `Environment` only stores variables. They do not bleed into each other. |
| **Loose Coupling** | Classes communicate through interfaces and contracts, not concrete implementations. The `Parser` does not know or care how tokens were produced. |
| **Open/Closed** | Adding a new instruction type (e.g., `while` loop) should require adding a new class, not modifying existing ones. |
| **Liskov Substitution** | Any `Expression` can be substituted for any other `Expression`. Any `Instruction` can be substituted for any other `Instruction`. The evaluator never needs to know which concrete type it holds. |
| **Separation of Concerns** | Lexical analysis (Tokenizer), syntactic analysis (Parser), semantic execution (Evaluator) are completely isolated. This is not just academic — it means bugs in one layer don't contaminate others. |

---

## 2. High-Level Architecture

```
Source Code (.zara file)
        │
        ▼
┌──────────────────┐
│    Tokenizer     │  CHARACTER stream → TOKEN stream
│                  │  Knows only: what characters exist
└────────┬─────────┘
         │  List<Token>
         ▼
┌──────────────────┐
│     Parser       │  TOKEN stream → INSTRUCTION TREE
│                  │  Knows only: grammar rules of ZARA
└────────┬─────────┘
         │  List<Instruction>
         ▼
┌──────────────────┐
│   Environment    │  Runtime variable store (shared state)
│                  │  Knows only: variable names → values
└────────┬─────────┘
         │  (injected into every Instruction.execute())
         ▼
┌──────────────────┐
│  Instruction     │  Walks the list, executes each node
│  Executor Loop   │  Knows only: call .execute(env)
└────────┬─────────┘
         │
         ▼
    Program Output (stdout)
```

The three stages are intentionally **hermetic** — they share data structures (tokens, instructions), not behavior. This allows each stage to be tested and reasoned about in complete isolation.

---

## 3. Project Directory Structure

```
zara-interpreter/
│
├── src/
│   └── main/
│       └── java/
│           └── zara/
│               │
│               ├── Main.java                        # CLI entry point
│               ├── Interpreter.java                 # Pipeline orchestrator
│               │
│               ├── lexer/                           # Stage 1: Tokenization
│               │   ├── TokenType.java               # Enum of all token kinds
│               │   ├── Token.java                   # Immutable token value object
│               │   └── Tokenizer.java               # Source → List<Token>
│               │
│               ├── ast/                             # Stage 2: Expression Tree Nodes
│               │   ├── Expression.java              # Interface — evaluate(Environment)
│               │   ├── NumberNode.java
│               │   ├── StringNode.java
│               │   ├── VariableNode.java
│               │   └── BinaryOpNode.java
│               │
│               ├── runtime/                         # Stage 3: Runtime state
│               │   └── Environment.java             # Variable store Map<String, Object>
│               │
│               ├── instruction/                     # Stage 3: Execution nodes
│               │   ├── Instruction.java             # Interface — execute(Environment)
│               │   ├── AssignInstruction.java
│               │   ├── PrintInstruction.java
│               │   ├── IfInstruction.java
│               │   └── RepeatInstruction.java
│               │
│               └── parser/                          # Stage 2: Token list → Instruction list
│                   └── Parser.java
│
├── test/
│   └── java/
│       └── zara/
│           ├── lexer/
│           │   └── TokenizerTest.java
│           ├── ast/
│           │   └── ExpressionEvalTest.java
│           ├── runtime/
│           │   └── EnvironmentTest.java
│           ├── instruction/
│           │   └── InstructionTest.java
│           └── integration/
│               └── InterpreterIntegrationTest.java  # Runs all 4 sample programs
│
├── samples/
│   ├── program1_arithmetic.zara
│   ├── program2_strings.zara
│   ├── program3_conditional.zara
│   └── program4_loop.zara
│
└── README.md
```

### Why this directory layout?

The package structure mirrors the pipeline stages. A developer reading `zara/lexer/` immediately knows they are looking at tokenization code, nothing else. The `ast/` package is purely data — no I/O, no side effects. The `instruction/` package is purely behavior. This is not cosmetic; it enforces that when you open a class, you already know what kind of thing it is.

---

## 4. The Pipeline: How Execution Flows

Understanding this end-to-end before writing any code is critical.

```
Input: "set result = x + y * 2"

STAGE 1 — Tokenizer produces:
  [KEYWORD:"set", IDENTIFIER:"result", ASSIGN:"=", IDENTIFIER:"x",
   PLUS:"+", IDENTIFIER:"y", STAR:"*", NUMBER:"2", NEWLINE, EOF]

STAGE 2 — Parser consumes tokens, builds:
  AssignInstruction {
      name: "result",
      expression: BinaryOpNode {
          left:  VariableNode { name: "x" },
          op:    "+",
          right: BinaryOpNode {
              left:  VariableNode { name: "y" },
              op:    "*",
              right: NumberNode { value: 2.0 }
          }
      }
  }

STAGE 3 — Evaluator calls:
  instruction.execute(env)
    → expression.evaluate(env)
      → BinaryOpNode(+).evaluate(env)
          left  = VariableNode("x").evaluate(env)  → env.get("x") → 10.0
          right = BinaryOpNode(*).evaluate(env)
              left  = VariableNode("y").evaluate(env) → env.get("y") → 3.0
              right = NumberNode(2.0).evaluate(env)   → 2.0
              result = 3.0 * 2.0 = 6.0
          result = 10.0 + 6.0 = 16.0
    → env.set("result", 16.0)

Output stored: result = 16.0
```

Notice: **operator precedence is handled entirely by tree shape**, not by any special logic. `*` sits deeper in the tree than `+`, so it evaluates first. The Parser is responsible for constructing this shape correctly via recursive descent.

---

## 5. Module-by-Module Design

### 5.1 Token Layer

**Package:** `zara.lexer`

#### `TokenType.java`

```java
public enum TokenType {
    // Literals
    NUMBER,        // 10, 3.14
    STRING,        // "hello"
    IDENTIFIER,    // x, result, score

    // ZARA Keywords
    SET,           // set
    SHOW,          // show
    WHEN,          // when
    LOOP,          // loop

    // Operators
    PLUS,          // +
    MINUS,         // -
    STAR,          // *
    SLASH,         // /
    ASSIGN,        // =
    GREATER,       // >
    LESS,          // <
    EQUALS,        // ==

    // Structure
    COLON,         // :
    NEWLINE,
    EOF
}
```

**Design note:** Keywords (`SET`, `SHOW`, `WHEN`, `LOOP`) are distinct token types — not just identifiers with a special string value. This means the Parser never has to do string comparisons like `if (token.getValue().equals("set"))`. Instead, it checks `token.getType() == TokenType.SET`. This is both more performant and more type-safe.

#### `Token.java`

```java
public final class Token {
    private final TokenType type;
    private final String value;
    private final int line;

    public Token(TokenType type, String value, int line) { ... }

    public TokenType getType()  { return type;  }
    public String    getValue() { return value; }
    public int       getLine()  { return line;  }

    @Override
    public String toString() {
        return String.format("Token(%s, \"%s\", line=%d)", type, value, line);
    }
}
```

**Why `final` class?** Tokens are pure **value objects** — immutable data carriers. They should never be subclassed. Marking the class `final` makes this contract explicit and allows the JVM to inline method calls. `toString()` is implemented for clean debugging output during development.

**Why store line number?** Even if you don't implement error messages for the base submission, storing the line in the Token costs almost nothing and makes debugging vastly easier. Every error message you might ever want is now possible.

---

### 5.2 Expression Tree (AST)

**Package:** `zara.ast`

The expression tree is the structural core of the interpreter. Every value computation — whether a literal, a variable reference, or an operation — is an `Expression` node.

#### `Expression.java` — The Core Interface

```java
public interface Expression {
    /**
     * Evaluates this expression in the context of the given environment.
     * Returns either a Double (numeric result) or a String (text result).
     * Implementations must never return null.
     */
    Object evaluate(Environment env);
}
```

**Why `Object` return type and not generics?**

This deserves a detailed answer. You might consider:

```java
// TEMPTING but wrong for this use case:
public interface Expression<T> {
    T evaluate(Environment env);
}
```

The problem is that at the `BinaryOpNode` level, you don't know at compile time whether `left` produces a `Double` or a `String`. ZARA allows `x + y` (numeric) and also string variables. The type is determined at *runtime* by what the variable holds. Generics are a compile-time tool — they can't capture runtime polymorphism here without getting into wildcards that become harder to work with than the `Object` approach. The `Object` return type, combined with explicit `instanceof` or casting in `BinaryOpNode`, is the correct trade-off here.

#### `NumberNode.java`

```java
public final class NumberNode implements Expression {
    private final double value;

    public NumberNode(double value) {
        this.value = value;
    }

    @Override
    public Object evaluate(Environment env) {
        return value;   // Returns Double (autoboxed)
    }
}
```

#### `StringNode.java`

```java
public final class StringNode implements Expression {
    private final String value;

    public StringNode(String value) {
        this.value = value;
    }

    @Override
    public Object evaluate(Environment env) {
        return value;
    }
}
```

#### `VariableNode.java`

```java
public final class VariableNode implements Expression {
    private final String name;

    public VariableNode(String name) {
        this.name = name;
    }

    @Override
    public Object evaluate(Environment env) {
        return env.get(name);   // Delegates entirely to Environment
    }
}
```

**Design note:** `VariableNode` does not contain any logic about what a variable *is*. It simply asks the `Environment`. This is the Dependency Inversion Principle in miniature — `VariableNode` depends on the `Environment` abstraction, not on a concrete storage mechanism.

#### `BinaryOpNode.java`

```java
public final class BinaryOpNode implements Expression {
    private final Expression left;
    private final String     operator;
    private final Expression right;

    public BinaryOpNode(Expression left, String operator, Expression right) {
        this.left     = left;
        this.operator = operator;
        this.right    = right;
    }

    @Override
    public Object evaluate(Environment env) {
        Object leftVal  = left.evaluate(env);
        Object rightVal = right.evaluate(env);

        // Both sides must be numeric for arithmetic/comparison
        double l = toDouble(leftVal);
        double r = toDouble(rightVal);

        return switch (operator) {
            case "+" -> l + r;
            case "-" -> l - r;
            case "*" -> l * r;
            case "/" -> l / r;
            case ">" -> l > r;
            case "<" -> l < r;
            case "==" -> l == r;
            default -> throw new RuntimeException("Unknown operator: " + operator);
        };
    }

    private double toDouble(Object val) {
        if (val instanceof Double d) return d;
        throw new RuntimeException(
            "Type error: expected number, got: " + val.getClass().getSimpleName()
        );
    }
}
```

**Why `switch` expression here?** Java 14+ switch expressions are both more readable and exhaustive — the compiler enforces that every case is handled or a default is present. This is better than a chain of `if/else if` blocks.

**Why does `BinaryOpNode` hold `Expression` references, not concrete types?** This is the **Composite Pattern**. A `BinaryOpNode` can hold *any* `Expression` as its children — including another `BinaryOpNode`. This is exactly what makes `x + y * 2` work: the right child of `+` is itself a `BinaryOpNode` for `*`. The tree composes recursively with zero additional code.

---

### 5.3 Environment (Variable Store)

**Package:** `zara.runtime`

```java
public class Environment {
    private final Map<String, Object> store = new HashMap<>();

    public void set(String name, Object value) {
        store.put(name, value);
    }

    public Object get(String name) {
        if (!store.containsKey(name)) {
            throw new RuntimeException("Variable not defined: " + name);
        }
        return store.get(name);
    }

    public boolean isDefined(String name) {
        return store.containsKey(name);
    }
}
```

**Why a dedicated class instead of passing a `Map` around directly?**

Three reasons:

1. **Encapsulation.** The `Environment` can enforce rules — like throwing a meaningful `RuntimeException` on undefined variable access — that a raw `Map` cannot. If you passed a `HashMap<String, Object>` directly, every call site would need to do its own null check.

2. **Future extensibility.** If you add scoping (e.g., variables inside a `loop` that shouldn't leak out), you change `Environment` in one place. If every class holds a direct reference to `HashMap`, you'd need to change every class.

3. **Testability.** You can mock or stub `Environment` in unit tests. You cannot mock a `HashMap`.

**Why `Map<String, Object>` and not `Map<String, Double>`?**

ZARA supports both numeric and string variables (`set name = "Sitare"`). Using `Object` as the value type is the honest representation of "this variable can hold a number or a string." Using `Double` would be a lie.

---

### 5.4 Instruction Layer

**Package:** `zara.instruction`

#### `Instruction.java` — Interface

```java
public interface Instruction {
    /**
     * Executes this instruction, reading and mutating state via env.
     * Side effects (printing, variable assignment) happen here.
     */
    void execute(Environment env);
}
```

**Why `void`?** Instructions don't produce values — they produce *effects*. `AssignInstruction` changes the `Environment`. `PrintInstruction` writes to stdout. `IfInstruction` may or may not execute its body. None of these need to return anything. The `void` return type is the correct contract.

#### `AssignInstruction.java`

```java
public final class AssignInstruction implements Instruction {
    private final String     variableName;
    private final Expression expression;

    public AssignInstruction(String variableName, Expression expression) {
        this.variableName = variableName;
        this.expression   = expression;
    }

    @Override
    public void execute(Environment env) {
        Object value = expression.evaluate(env);
        env.set(variableName, value);
    }
}
```

#### `PrintInstruction.java`

```java
public final class PrintInstruction implements Instruction {
    private final Expression expression;

    public PrintInstruction(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void execute(Environment env) {
        Object value = expression.evaluate(env);
        // Format doubles cleanly: 16.0 → "16", not "16.0"
        if (value instanceof Double d && d == Math.floor(d) && !Double.isInfinite(d)) {
            System.out.println(d.intValue());
        } else {
            System.out.println(value);
        }
    }
}
```

**Design note on number formatting:** ZARA programs like `show result` where `result = 16.0` should print `16`, not `16.0`. Integers in ZARA are conceptually whole numbers. This formatting concern belongs in `PrintInstruction`, not in `Environment` or `BinaryOpNode`. This is high cohesion — the only class that cares how a value is *displayed* is the one responsible for displaying it.

#### `IfInstruction.java`

```java
public final class IfInstruction implements Instruction {
    private final Expression        condition;
    private final List<Instruction> body;

    public IfInstruction(Expression condition, List<Instruction> body) {
        this.condition = condition;
        this.body      = List.copyOf(body);   // Defensive copy — immutable after construction
    }

    @Override
    public void execute(Environment env) {
        Object result = condition.evaluate(env);
        if (result instanceof Boolean b && b) {
            for (Instruction instruction : body) {
                instruction.execute(env);
            }
        }
    }
}
```

**Why `List.copyOf()`?** The `List<Instruction>` passed to the constructor might be the same mutable list the Parser is still building. If the Parser later modifies it, `IfInstruction` would silently hold corrupted state. Defensive copying prevents this class of bug entirely.

**Why `instanceof Boolean b && b`?** This uses Java's pattern matching for `instanceof` (Java 16+). It simultaneously checks that the result is a `Boolean` *and* unpacks it into `b` in one step. More concise and more correct than `if (result.equals(Boolean.TRUE))`.

#### `RepeatInstruction.java`

```java
public final class RepeatInstruction implements Instruction {
    private final int             count;
    private final List<Instruction> body;

    public RepeatInstruction(int count, List<Instruction> body) {
        this.count = count;
        this.body  = List.copyOf(body);
    }

    @Override
    public void execute(Environment env) {
        for (int i = 0; i < count; i++) {
            for (Instruction instruction : body) {
                instruction.execute(env);
            }
        }
    }
}
```

---

### 5.5 Tokenizer

**Package:** `zara.lexer`

The Tokenizer has one job: transform a raw `String` into a `List<Token>`. It must handle:

- Whitespace (skip spaces and tabs, but not newlines)
- Newlines (emit a `NEWLINE` token — important for block detection)
- Numbers (integers and decimals)
- Strings (quoted text including spaces)
- Identifiers (which may be keywords)
- Operators (single or double character: `=`, `==`, `>`, `<`, `+`, `-`, `*`, `/`)
- End of file (emit a single `EOF` token)

**Conceptual structure:**

```java
public class Tokenizer {
    private final String source;
    private int pos;     // current character index
    private int line;    // current line number (starts at 1)

    public Tokenizer(String source) {
        this.source = source;
        this.pos    = 0;
        this.line   = 1;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (pos < source.length()) {
            skipWhitespace();       // skip spaces/tabs only
            if (pos >= source.length()) break;

            char c = source.charAt(pos);

            if (c == '\n') {
                tokens.add(new Token(TokenType.NEWLINE, "\n", line));
                line++;
                pos++;
            } else if (c == '"') {
                tokens.add(readString());
            } else if (Character.isDigit(c)) {
                tokens.add(readNumber());
            } else if (Character.isLetter(c) || c == '_') {
                tokens.add(readIdentifierOrKeyword());
            } else {
                tokens.add(readOperator());
            }
        }
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    // Private helper methods: readString(), readNumber(),
    // readIdentifierOrKeyword(), readOperator(), skipWhitespace(), peek(), advance()
}
```

**Key design decisions:**

- **`pos` is the only mutable state.** The source string is immutable. This means the Tokenizer's logic is purely a function of `source` and `pos` — no hidden state, easy to reason about and test.
- **`line` tracking is cheap and invaluable.** Increment `line` on every `\n` character. This is O(1) work per character.
- **Keyword detection happens in `readIdentifierOrKeyword()`.** Read the full identifier first, *then* check if it matches a keyword. Do not try to detect keywords character by character.

```java
private Token readIdentifierOrKeyword() {
    int start = pos;
    while (pos < source.length() &&
           (Character.isLetterOrDigit(source.charAt(pos)) || source.charAt(pos) == '_')) {
        pos++;
    }
    String word = source.substring(start, pos);
    TokenType type = switch (word) {
        case "set"  -> TokenType.SET;
        case "show" -> TokenType.SHOW;
        case "when" -> TokenType.WHEN;
        case "loop" -> TokenType.LOOP;
        default     -> TokenType.IDENTIFIER;
    };
    return new Token(type, word, line);
}
```

**Why a `switch` expression for keyword mapping?** The JVM compiles `switch` on strings using a hash map internally — O(1) lookup. It is also more readable and maintainable than a chain of `if/else if` comparisons.

---

### 5.6 Parser

**Package:** `zara.parser`

The Parser is the most algorithmically complex component. It reads tokens sequentially and builds the instruction tree using **recursive descent parsing**.

#### The Operator Precedence Problem

This is the most important conceptual challenge in the Parser. Consider `x + y * 2`. If we parse left-to-right naively, we get `(x + y) * 2 = 26`. The correct answer is `x + (y * 2) = 16`. The Parser must build a tree that reflects `*` having higher precedence than `+`.

**The solution: a call chain where lower-precedence rules call higher-precedence rules.**

```
parseExpression()   →  handles + and - (lowest precedence)
    calls
parseTerm()         →  handles * and / (medium precedence)
    calls
parsePrimary()      →  handles a single value (highest precedence / base case)
```

This chain means `*` and `/` are always consumed *before* `+` and `-` are processed, naturally producing the correct tree shape.

```java
// Conceptual sketch — not complete implementation

private Expression parseExpression() {
    Expression left = parseTerm();                  // Consume * and / first

    while (current().getType() == TokenType.PLUS ||
           current().getType() == TokenType.MINUS  ||
           current().getType() == TokenType.GREATER ||
           current().getType() == TokenType.LESS) {
        String op = consume().getValue();
        Expression right = parseTerm();
        left = new BinaryOpNode(left, op, right);  // Left-associative: (a+b)+c
    }
    return left;
}

private Expression parseTerm() {
    Expression left = parsePrimary();

    while (current().getType() == TokenType.STAR ||
           current().getType() == TokenType.SLASH) {
        String op = consume().getValue();
        Expression right = parsePrimary();
        left = new BinaryOpNode(left, op, right);
    }
    return left;
}

private Expression parsePrimary() {
    Token t = consume();
    return switch (t.getType()) {
        case NUMBER     -> new NumberNode(Double.parseDouble(t.getValue()));
        case STRING     -> new StringNode(t.getValue());
        case IDENTIFIER -> new VariableNode(t.getValue());
        default         -> throw new RuntimeException(
                              "Unexpected token in expression: " + t + " on line " + t.getLine());
    };
}
```

#### Parsing a ZARA Block (Indented Body)

ZARA uses indented blocks after `when` and `loop`. The Parser needs to know when the block starts and ends. The simplest approach: after consuming the colon `(:)`, read instructions until you hit a line that is not indented (or `EOF`).

```
when z > 30:          ← WHEN token, condition, COLON
    show "big number" ← body (indented line)
show "done"           ← NOT indented → block ends
```

The Parser can track block depth by checking whether the current line starts with whitespace (or by counting NEWLINE tokens and peeking at what follows).

#### Parser State

```java
public class Parser {
    private final List<Token> tokens;
    private int index;            // Points to current token

    private Token current() { return tokens.get(index); }
    private Token consume()  { return tokens.get(index++); }
    private Token expect(TokenType type) {
        if (current().getType() != type) {
            throw new RuntimeException(
                "Expected " + type + " but got " + current() + " on line " + current().getLine()
            );
        }
        return consume();
    }
}
```

**Why `expect()` instead of just `consume()`?** `expect()` documents the *contract* — "this token must be present here." If it is not, you get a clear error message with the expected type, actual type, and line number. Failing silently by just advancing past a wrong token produces a bug that is extremely hard to trace back to its source.

---

### 5.7 Evaluator / Interpreter Entry Point

**Package:** `zara` (root)

```java
public class Interpreter {
    public void run(String sourceCode) {
        // Stage 1: Lex
        Tokenizer tokenizer  = new Tokenizer(sourceCode);
        List<Token> tokens   = tokenizer.tokenize();

        // Stage 2: Parse
        Parser parser              = new Parser(tokens);
        List<Instruction> program  = parser.parse();

        // Stage 3: Execute
        Environment env = new Environment();
        for (Instruction instruction : program) {
            instruction.execute(env);
        }
    }
}
```

**Why is this class so small?** Because it is a pipeline orchestrator, not a logic container. The moment `Interpreter` starts containing parsing or evaluation logic, you have violated separation of concerns. If `Interpreter` is ever more than 20 lines, reconsider what logic has leaked into it.

```java
public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: java -cp . zara.Main <file.zara>");
            System.exit(1);
        }
        String sourceCode = Files.readString(Path.of(args[0]));
        new Interpreter().run(sourceCode);
    }
}
```

---

## 6. Design Principles Applied

### The Composite Pattern (Expression Tree)

`BinaryOpNode` holds two `Expression` references. Those can be `NumberNode`, `VariableNode`, or *another* `BinaryOpNode`. The tree composes arbitrarily deep with no additional code. This is the Composite Pattern.

```
x + y * (z - 2)

BinaryOpNode(+)
├── VariableNode(x)
└── BinaryOpNode(*)
    ├── VariableNode(y)
    └── BinaryOpNode(-)
        ├── VariableNode(z)
        └── NumberNode(2)
```

### The Command Pattern (Instructions)

`AssignInstruction`, `PrintInstruction`, `IfInstruction`, `RepeatInstruction` all implement `Instruction`. The executor loop does not need to know what kind of instruction it holds — it calls `.execute(env)` on each one. This is the Command Pattern: encapsulating a behavior as an object.

### The Visitor Pattern (Implicit)

`Expression.evaluate(Environment env)` passing the `env` down through the tree is an implicit form of visitor traversal. The tree is walked, and at each node, the `env` is passed in to provide context. The tree nodes do not hold mutable state — they receive it each time they are evaluated. This makes expressions **referentially transparent**: the same `NumberNode(42)` always returns `42` regardless of when or how many times it is called.

### The Pipeline / Chain of Responsibility Pattern

`Tokenizer → Parser → Executor` is a strict pipeline. No stage knows about the stages before or after it. Each consumes a well-typed input and produces a well-typed output.

---

## 7. Key Design Decisions & Why

### Why all Expression nodes are `final`

Node classes represent pure data. They should not be subclassed — their behavior is fully defined. Marking them `final` is a statement: "there is no other kind of `NumberNode`." It also prevents the subtle bug where someone subclasses `NumberNode` and overrides `evaluate()` in an unexpected way.

### Why `List<Instruction>` not a single root `Instruction`

A ZARA program is a flat sequence of instructions, not a single tree root. Wrapping the list in a `ProgramNode` class would be unnecessary indirection. The `List<Instruction>` *is* the program.

### Why `HashMap` inside `Environment`

`HashMap` gives O(1) average-case lookup and insert for variable access. Variable lookups happen on every expression evaluation — performance here matters. A `LinkedHashMap` would also work if insertion-order iteration is ever needed.

### Why not use inheritance between `Expression` nodes

`NumberNode`, `StringNode`, `VariableNode`, `BinaryOpNode` share nothing implementation-wise. An abstract base class `ExpressionNode` with no real behavior is just noise. The `Expression` interface captures the *contract* without forcing fake inheritance. Prefer interfaces over abstract classes when there is no shared behavior to inherit.

### Why `Environment` is not a singleton

Singletons are global mutable state. If `Environment` were a singleton, you could not run two ZARA programs concurrently, you could not test instructions in isolation (they'd mutate shared state), and you'd never be able to add scoping. Passing `env` explicitly is the correct approach — it makes dependencies visible and testable.

---

## 8. Error Handling Strategy

Error handling lives at two levels:

**Parse errors** — the source code is structurally invalid (missing colon after `when`, unknown token). These should throw a custom `ParseException` with the line number.

**Runtime errors** — an undefined variable is accessed, or an arithmetic operation is given a string. These throw a `RuntimeException` with a descriptive message.

```java
// Custom exception for parse-time errors
public class ParseException extends RuntimeException {
    private final int line;

    public ParseException(String message, int line) {
        super("Line " + line + ": " + message);
        this.line = line;
    }
}
```

**Where errors are thrown:**
- `Environment.get()` — undefined variable
- `BinaryOpNode.evaluate()` — type mismatch
- `Parser.expect()` — unexpected token
- `parsePrimary()` — unexpected token in expression

**Where errors are caught:**
- `Interpreter.run()` — top-level catch, prints the error and exits cleanly

This means error-handling code is in exactly one place, not scattered across every method.

---

## 9. Extension Points

The design is open for extension without modification:

| Feature | How to add it |
|---------|---------------|
| `else` block | Add `elseBody: List<Instruction>` field to `IfInstruction`. Parser reads it if `else` token follows the `when` block. No other changes. |
| `while` loop | New class `WhileInstruction implements Instruction`. New `TokenType.WHILE`. Parser handles it. Zero changes to existing classes. |
| `==` equality | Add `TokenType.EQUALS`, handle `"=="` in `BinaryOpNode.evaluate()`. One case in a switch. |
| String length | New `LengthNode implements Expression` or handle a `length(x)` syntax in `parsePrimary()`. |
| Nested blocks | Already works. `IfInstruction.body` is `List<Instruction>` — it can contain another `IfInstruction` or `RepeatInstruction`. The recursive structure handles it for free. |

---

## 10. What NOT To Do

These are anti-patterns that are tempting in early designs but create problems that compound.

**❌ Don't put evaluation logic inside the Parser.**  
The Parser builds the tree. The tree evaluates itself. If your Parser is calling `env.set()` or doing arithmetic, the stages are not separated.

**❌ Don't use `String` comparisons for token type checks.**  
`if (token.getValue().equals("set"))` is fragile. Use `token.getType() == TokenType.SET`. You defined the enum for this reason.

**❌ Don't store `Environment` as a class-level field in expression nodes.**  
`Expression.evaluate(Environment env)` receives the environment as a parameter. If you store it in the node at parse time, you have baked the environment into the syntax tree — these are different things (structure vs. state) and must not be conflated.

**❌ Don't make `Token` mutable.**  
Tokens are created once and read many times. A mutable token is a data integrity hazard. All fields must be `final`, set in the constructor.

**❌ Don't implement a `switch` on `instanceof` in the executor loop.**  
```java
// WRONG: This is what interfaces are for
if (instruction instanceof AssignInstruction a) { ... }
else if (instruction instanceof PrintInstruction p) { ... }
```  
Call `instruction.execute(env)` and let polymorphism do its job. That is the entire point of the `Instruction` interface.

**❌ Don't build and test the whole interpreter at once.**  
Build in this order: `Token` → `Tokenizer` → leaf `Expression` nodes → `Environment` → `BinaryOpNode` → `AssignInstruction` + `PrintInstruction` → `Parser` for simple expressions → `IfInstruction` → `RepeatInstruction`. Test each stage before moving to the next.

---

*This document describes the engineering design of the ZARA interpreter. It defines structure, rationale, and contracts — not implementation. The implementation follows from this design.*
