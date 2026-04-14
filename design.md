# ZARA Interpreter — System Design Document

> **Language:** ZARA (Zero-ceremony Arithmetic and Reasoning Assembler)
> **Course:** Advanced OOP in Java — Sitare University
> **Team:** Group 29
> **Document Type:** Engineering Design & Architecture (v3)

---

## Table of Contents

1. [Philosophy & Design Goals](#1-philosophy--design-goals)
2. [High-Level Architecture](#2-high-level-architecture)
3. [Project Directory Structure](#3-project-directory-structure)
4. [The Pipeline: How Execution Flows](#4-the-pipeline-how-execution-flows)
5. [Module-by-Module Design](#5-module-by-module-design)
   - 5.1 [Lexer Layer (Token + Tokenizer)](#51-lexer-layer-token--tokenizer)
   - 5.2 [Expression Tree (AST)](#52-expression-tree-ast)
   - 5.3 [Parser Layer](#53-parser-layer)
   - 5.4 [Instruction Layer](#54-instruction-layer)
   - 5.5 [Runtime Layer (Environment + Interpreter)](#55-runtime-layer-environment--interpreter)
   - 5.6 [Entry Point (Main)](#56-entry-point-main)
6. [SOLID Principles Applied](#6-solid-principles-applied)
7. [Key Design Decisions & Why](#7-key-design-decisions--why)
8. [Error Handling Strategy (Fail-Fast Approach)](#8-error-handling-strategy-fail-fast-approach)
9. [Extension Points](#9-extension-points)
10. [What NOT To Do](#10-what-not-to-do)

---

## How to Read This Document

This is a **design document**, not an implementation guide. It describes:

- What each component is responsible for
- What contract (interface/signature) it exposes
- Why each design decision was made

It does **not** describe how to write every method body — the reasoning sections explain the thinking, and translating that thinking into working code is the implementation work that has been done by the team.

---

## 1. Philosophy & Design Goals

Before writing a single line of code, the team agreed on what kind of system this is. A scripting engine interpreter is not a data-processing app — it is a **pipeline of transformations**, where each stage produces a well-defined output that the next stage consumes. The design reflects that.

### Core Engineering Goals

| Goal | What it means in practice |
|------|---------------------------|
| **High Cohesion** | Every class has one clear job. `Tokenizer` only tokenizes. `Environment` only stores variables. They do not bleed into each other. |
| **Loose Coupling** | Classes communicate through interfaces and contracts, not concrete implementations. The `Parser` does not know or care how tokens were produced. |
| **Open/Closed** | Adding a new instruction type (e.g., a `while` loop) should require adding a new class, not modifying existing ones. |
| **Liskov Substitution** | Any `Expression` can stand in for any other `Expression`. Any `Instruction` can stand in for any other `Instruction`. The executor loop never needs to know which concrete type it holds. |
| **Separation of Concerns** | Lexical analysis (Tokenizer), syntactic analysis (Parser), and semantic execution (Executor) are completely isolated layers. Bugs in one layer cannot contaminate others. |

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
│     Parser       │  TOKEN stream → INSTRUCTION LIST
│                  │  Knows only: grammar rules of ZARA
└────────┬─────────┘
         │  List<Instruction>
         ▼
┌──────────────────────────┐
│  Interpreter Executor    │  Walks the instruction list
│  (in Interpreter.run())  │  Calls .execute(env) on each
└────────┬─────────────────┘
         │  reads/writes
         ▼
┌──────────────────┐
│   Environment    │  Runtime variable store
│                  │  variable name → current value
└──────────────────┘
         │
         ▼
    Program Output (stdout)
```

The three stages — tokenizing, parsing, executing — are intentionally **hermetic**. They share data structures (tokens, instructions), not behavior. Each can be built, tested, and reasoned about in complete isolation.

---

## 3. Project Directory Structure

```
zara-interpreter-g29/
│
├── src/main/java/zara/
│   │
│   ├── Main.java                     # CLI entry point
│   │
│   ├── ast/                          # Expression tree nodes (pure data, no I/O)
│   │   ├── Expression.java           # Interface — evaluate(Environment) → Object
│   │   ├── NumberNode.java           # Literal number
│   │   ├── StringNode.java           # Literal string
│   │   ├── VariableNode.java         # Variable reference
│   │   └── BinaryOpNode.java         # Two expressions joined by an operator
│   │
│   ├── lexer/                        # Stage 1: Tokenization
│   │   ├── TokenType.java            # Enum of all token types
│   │   ├── Token.java                # Immutable value object
│   │   └── Tokenizer.java            # Reads source String → List<Token>
│   │
│   ├── parser/                       # Stage 2: tokens → instructions
│   │   ├── Parser.java               # List<Token> → List<Instruction>
│   │   └── ParseException.java       # Parser-specific error type
│   │
│   ├── instruction/                  # Stage 3: Executable instruction nodes
│   │   ├── Instruction.java          # Interface — execute(Environment)
│   │   ├── AssignInstruction.java    # set x = <expr>
│   │   ├── PrintInstruction.java     # show <expr>
│   │   ├── IfInstruction.java        # when <cond>: <block>
│   │   ├── RepeatInstruction.java    # loop <n>: <block>
│   │   └── BlockInstruction.java     # Sequential instruction container
│   │
│   └── runtime/                      # Runtime state & orchestration
│       ├── Environment.java          # Variable store (name → value)
│       └── Interpreter.java          # Pipeline orchestrator
│
├── test/
│   │
│   ├── program1.zara                 # Arithmetic & variables
│   ├── program2.zara                 # String output
│   ├── program3.zara                 # Conditional (when)
│   ├── program4.zara                 # Loop
│   │
│   └── java/zara/
│       │
│       ├── ast/
│       │   └── ExpressionEvalTest.java
│       │
│       ├── lexer/
│       │   └── TokenizerTest.java
│       │
│       ├── parser/
│       │   └── ParserTest.java
│       │
│       ├── instruction/
│       │   ├── AssignInstructionTest.java
│       │   ├── PrintInstructionTest.java
│       │   ├── IfInstructionTest.java
│       │   ├── RepeatInstructionTest.java
│       │   └── BlockInstructionTest.java
│       │
│       └── runtime/
│           ├── EnvironmentTest.java
│           └── InterpreterIntegrationTest.java
│
├── .gitignore
├── design.md
├── code_explanation.txt
└── README.md
```

### Why this directory layout?

The package structure mirrors the pipeline stages directly. When you open `zara/lexer/`, you are looking at tokenization code only. The `ast/` package is pure data — no I/O, no side effects. The `instruction/` package is pure behavior. The `runtime/` package holds everything that persists during execution — the Environment and the Interpreter that drives it. This is not cosmetic; it enforces that every class has a clear home and a clear job.

---

## 4. The Pipeline: How Execution Flows

Understanding this end-to-end is essential.

**Example input line:** `set result = x + y * 2`

**Stage 1 — Tokenizer output:**
```
SET | IDENTIFIER("result") | ASSIGN | IDENTIFIER("x") |
PLUS | IDENTIFIER("y") | STAR | NUMBER("2") | NEWLINE
```

**Stage 2 — Parser output (tree structure):**
```
AssignInstruction
  name: "result"
  expression:
    BinaryOpNode("+")
      left:  VariableNode("x")
      right: BinaryOpNode("*")
                left:  VariableNode("y")
                right: NumberNode(2.0)
```

**Stage 3 — Execution (assuming x=10, y=3 in Environment):**
```
AssignInstruction.execute(env)
  → expression.evaluate(env)
    → BinaryOpNode("+").evaluate(env)
        left  → VariableNode("x").evaluate(env) → env.get("x") → 10.0
        right → BinaryOpNode("*").evaluate(env)
                  left  → VariableNode("y").evaluate(env) → 3.0
                  right → NumberNode(2.0).evaluate(env)   → 2.0
                  result = 3.0 * 2.0 = 6.0
        result = 10.0 + 6.0 = 16.0
  → env.set("result", 16.0)
```

The key insight here is that **operator precedence is handled entirely by tree shape** — `*` sits deeper in the tree than `+`, so it evaluates first. No special-case logic is needed anywhere. The Parser is responsible for constructing this correct shape.

---

## 5. Module-by-Module Design

### 5.1 Lexer Layer (Token + Tokenizer)

**Package:** `zara.lexer`

#### `TokenType.java`

An enum listing every distinct kind of token ZARA can produce. Every team member should be able to read this enum and immediately understand the full vocabulary of the language.

Required entries:
- Literals: `NUMBER`, `STRING`, `IDENTIFIER`
- ZARA keywords: `SET`, `SHOW`, `WHEN`, `LOOP`
- Arithmetic operators: `PLUS`, `MINUS`, `STAR`, `SLASH`
- Comparison & assignment: `ASSIGN` (=), `EQUALS` (==), `GREATER` (>), `LESS` (<)
- Structure: `COLON`, `NEWLINE`, `EOF`

**Why keywords get their own enum values (not just IDENTIFIER):**
The Parser must distinguish `set` from a variable named `x`. If both were `IDENTIFIER`, the Parser would need to do `token.getValue().equals("set")` everywhere — fragile string comparison. With `TokenType.SET`, the check is `token.getType() == TokenType.SET` — type-safe and impossible to misspell silently.

#### `Token.java`

Fields (all `private final`, set in constructor, no setters):
- `TokenType type` — what kind of token this is
- `String value` — the raw text from the source
- `int line` — which line of source code this came from

Getters for all three fields. `toString()` implemented for debugging.

**Why `final` class:** Tokens are pure value objects — immutable data carriers. There is no meaningful subtype of "a token."

#### `Tokenizer.java`

The Tokenizer reads the raw source `String` character by character and produces a `List<Token>`.

State:
- `source` (set once in constructor, never changed)
- `pos` (advances as characters are consumed)
- `line` (incremented on every newline, used to populate `Token.line`)

What `tokenize()` handles:

| Input pattern | Output |
|---|---|
| Space or tab | Skip — do not emit a token |
| `\n` | Emit `NEWLINE` token, increment line counter |
| `"` | Read until the closing `"`, emit `STRING` token |
| Digit | Read all consecutive digit/dot characters, emit `NUMBER` token |
| Letter or `_` | Read the word; check against keyword list; emit appropriate type |
| `=` | Peek next — if `=`, emit `EQUALS`; otherwise emit `ASSIGN` |
| `+` `-` `*` `/` `>` `<` `:` | Emit corresponding operator token |
| End of input | Emit one `EOF` token |

**Why `pos` is the only mutable driver of progress:** If the source is immutable and `pos` monotonically increases, the Tokenizer's behavior is entirely determined by those two things — easy to trace, easy to test.

---

### 5.2 Expression Tree (AST)

**Package:** `zara.ast`

Every value computation in ZARA is represented as a node object. All node classes implement the `Expression` interface.

#### `Expression.java` — Interface contract

```java
public interface Expression {
    Object evaluate(Environment env);
}
```

This single method is the entire contract. Every node must be able to evaluate itself given the current variable store and return either a `Double` (numbers), `String` (text), or `Boolean` (comparisons).

**Why `Object` return type and not generics:**
At `BinaryOpNode`, you don't know at compile time whether the left side produces a `Double` or a `String` — that depends on what the variable holds at runtime. Generics are a compile-time tool and cannot capture this runtime variability. `Object` is the honest representation.

#### Node classes

**`NumberNode`** — Holds a single `double` value. `evaluate()` returns it. Does not touch `env`.

**`StringNode`** — Holds a single `String` value. `evaluate()` returns it. Does not touch `env`.

**`VariableNode`** — Holds a variable name. `evaluate()` delegates to `env.get(name)`.

**`BinaryOpNode`** — Holds a left `Expression`, an operator `String`, and a right `Expression`. `evaluate()` recursively evaluates both sides, then applies the operator.
- Arithmetic (`+`, `-`, `*`, `/`) → returns `Double`
- Comparison (`>`, `<`, `==`) → returns `Boolean`
- Invalid operand types → throws `RuntimeException`

**Why `BinaryOpNode` holds `Expression` references, not concrete node types:**
This is what makes the tree composable. The right child of a `+` node can itself be a `*` node — which is exactly how `x + y * 2` gets the correct structure. This is the **Composite Pattern**.

---

### 5.3 Parser Layer

**Package:** `zara.parser`

The Parser reads the `List<Token>` from the Tokenizer and builds a `List<Instruction>`. It is the most algorithmically complex component.

#### `Parser.java`

State:
- The full token list (set once in constructor)
- A current index into that list (advances as tokens are consumed)
- Helper methods: `peek()`, `advance()`, `check(TokenType)`, `consume(TokenType)`

#### The `parse()` flow

`parse()` is a loop: while the current token is not `EOF`, look at the current token type and dispatch to the appropriate handler:

```
if current token is SET  → parseAssign()   → produces AssignInstruction
if current token is SHOW → parsePrint()    → produces PrintInstruction
if current token is WHEN → parseWhen()     → produces IfInstruction
if current token is LOOP → parseLoop()     → produces RepeatInstruction
if current token is NEWLINE → skip, advance
otherwise → throw ParseException
```

#### The operator precedence problem

Consider `x + y * 2`. Parsed naively left-to-right: `(x + y) * 2 = 26`. Correct result: `x + (y * 2) = 16`.

**The solution is a three-level call chain:**

```
parseExpression()   handles: + − > < ==    (lowest precedence)
    └── calls parseTerm() to get each operand

parseTerm()         handles: * /            (higher precedence)
    └── calls parsePrimary() to get each operand

parsePrimary()      handles: a single value (highest precedence / base case)
    returns: NumberNode, StringNode, or VariableNode
```

Walk through `x + y * 2`:
1. `parseExpression` calls `parseTerm` → which calls `parsePrimary` → returns `VariableNode("x")`
2. `parseTerm` sees no `*` or `/`, returns `VariableNode("x")` to `parseExpression`
3. `parseExpression` sees `+`, consumes it, calls `parseTerm` again for the right side
4. `parseTerm` calls `parsePrimary` → returns `VariableNode("y")`
5. `parseTerm` sees `*`, consumes it, calls `parsePrimary` → returns `NumberNode(2.0)`
6. `parseTerm` wraps: `BinaryOpNode("*", VariableNode("y"), NumberNode(2.0))`
7. `parseExpression` wraps: `BinaryOpNode("+", VariableNode("x"), BinaryOpNode("*", ...))`

Result: `*` sits deeper than `+`. Correct tree. No special logic needed.

#### `ParseException.java`

A custom `RuntimeException` for parser-specific errors. Carries a line number so error messages are immediately actionable: `"Line 4: expected ':' after condition"`.

---

### 5.4 Instruction Layer

**Package:** `zara.instruction`

An instruction is one complete, executable action. All instruction classes implement the `Instruction` interface.

#### `Instruction.java` — Interface contract

```java
public interface Instruction {
    void execute(Environment env);
}
```

**Why `void`:** Instructions produce *effects*, not values. Assignment changes the `Environment`. Print writes to stdout. A conditional may or may not execute its body. None need to return anything.

#### Instruction classes

**`AssignInstruction`** — Handles `set x = <expr>`
- Holds: variable name (`String`), expression (`Expression`)
- Behavior: evaluate expression, store result in `env`
- Fail-fast: throws `IllegalArgumentException` on null/empty variable name or null expression

**`PrintInstruction`** — Handles `show <expr>`
- Holds: expression to print (`Expression`)
- Behavior: evaluate, format, print to stdout
- Formatting: whole numbers print as integers (`16.0` → `16`), not floats
- Formatting logic isolated in private `format()` helper (Single Responsibility)

**`IfInstruction`** — Handles `when <cond>: <block>`
- Holds: condition (`Expression`), body (`List<Instruction>`)
- Behavior: evaluate condition (must be `Boolean`); if `true`, execute body
- Fail-fast: throws if condition does not evaluate to a boolean

**`RepeatInstruction`** — Handles `loop <n>: <block>`
- Holds: count (`int`), body (`List<Instruction>`)
- Behavior: execute body `count` times
- Fail-fast: throws `IllegalArgumentException` on negative count or empty body

**`BlockInstruction`** — Sequential container
- Holds: a `List<Instruction>` to execute in order
- Behavior: iterate and call `execute()` on each
- Used internally to group statements in `when` and `loop` bodies

**Why the body is `List<Instruction>` and not a dedicated class:**
An `IfInstruction` body can contain another `IfInstruction` or `RepeatInstruction`. Nesting works automatically through this recursive structure — no special case required.

---

### 5.5 Runtime Layer (Environment + Interpreter)

**Package:** `zara.runtime`

#### `Environment.java`

The variable store — a `Map<String, Object>` from variable names to current values. One instance is created per program run and shared across every instruction's execution.

```java
public class Environment {
    private final Map<String, Object> variables = new HashMap<>();

    public void set(String name, Object value);
    public Object get(String name);   // throws RuntimeException if undefined
}
```

**Why a dedicated class instead of passing a `Map` directly:**
A raw `Map<String, Object>` has no rules. `Environment` is a typed contract with enforced behavior: undefined variable access throws a meaningful error rather than returning `null`. Centralizing this logic means it happens in exactly one place.

**Why `Map<String, Object>` and not `Map<String, Double>`:**
ZARA variables can hold either numbers or strings (`set name = "Sitare"` is valid). `Object` is the correct type.

**Why not a singleton:**
A singleton is global mutable state. It would make isolated testing impossible, would prevent running two programs concurrently, and would make scoped variables difficult to add. Passing `env` as a parameter makes the dependency explicit and testable.

#### `Interpreter.java`

```java
public class Interpreter {
    public void run(String sourceCode) {
        // Step 1: Tokenize  — Tokenizer → List<Token>
        // Step 2: Parse     — Parser    → List<Instruction>
        // Step 3: Execute   — new Environment, for each instruction: execute(env)
    }
}
```

This class is a pipeline orchestrator. Its only job is to connect the three stages. If it ever contains parsing logic, evaluation logic, or arithmetic — the separation of concerns has failed.

---

### 5.6 Entry Point (Main)

**Package:** `zara`

`Main.java` is the CLI entry point. Responsibilities:
1. Read the source file path from command-line arguments
2. Read file contents into a `String`
3. Normalize line endings (handle Windows CRLF for cross-platform support)
4. Strip BOM if present
5. Call `new Interpreter().run(sourceCode)`
6. Catch `IOException` (file read errors) and `RuntimeException` (interpreter errors) at top level, print a clean message, exit

**Why cross-platform cleanup lives in Main:**
File reading is an I/O boundary. Normalizing input at the boundary means the Tokenizer only ever sees clean `\n`-separated source — it doesn't need to know the file came from a Windows editor. This is the Robustness Principle applied at the right place.

---

## 6. SOLID Principles Applied

This project strictly adheres to major Object-Oriented design principles to assure a scalable and decoupled architecture.

### Single Responsibility Principle (SRP)
Every class has exactly one job:
- `Tokenizer` ONLY reads characters to produce tokens (no grammar checking).
- `Environment` ONLY stores variables and handles missing-variable errors.
- `BinaryOpNode` ONLY evaluates its operation (no side effects like printing).
- `PrintInstruction.format()` is isolated as a private helper so `execute()` does not mix concerns.

### Open/Closed Principle (OCP)
The architecture is open for extension but closed for modification. Adding a `while` loop or a function definition does not require modifying `AssignInstruction` or `IfInstruction` — new instructions are added as new sibling classes. The executor loop never needs to change.

### Liskov Substitution Principle (LSP)
Any concrete implementation of an interface can seamlessly replace any other. `AssignInstruction` expects an `Expression`. It does not matter if that expression is a primitive `NumberNode` or a deeply nested `BinaryOpNode` — both honor the `evaluate()` contract identically.

### Interface Segregation Principle (ISP)
Interfaces are kept hyper-focused and minimal:
- `Expression` demands only `Object evaluate(Environment env)`.
- `Instruction` demands only `void execute(Environment env)`.

No class is forced to implement methods it does not need.

### Dependency Inversion Principle (DIP)
High-level policy modules (`Interpreter.java`, the executor loop) depend on the `Instruction` and `Expression` abstractions, not on concrete classes like `AssignInstruction`. This is why the executor loop is just a `for` loop calling `.execute()` — it is decoupled from every concrete instruction class.

### Supporting Patterns

**Composite Pattern — Expression Tree**
`BinaryOpNode` holds two `Expression` children. Those children can themselves be `BinaryOpNode` instances. The tree composes arbitrarily deep. No special code handles nesting — it emerges from the structure.

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

**Command Pattern — Instructions**
Every instruction is an object that knows how to execute itself. The executor loop in `Interpreter.run()` does not know or care whether it is executing an assignment, a print, a conditional, or a loop. It calls `.execute(env)` and the object handles the rest.

**Pipeline Pattern — Three Isolated Stages**
`Tokenizer → Parser → Executor` is a strict pipeline. No stage holds a reference to any other. Each consumes a well-typed input and produces a well-typed output. Each can be tested in complete isolation.

---

## 7. Key Design Decisions & Why

### Why all Expression node fields are `private final`
Nodes represent fixed, well-defined values. A `NumberNode` *is* a number. Immutability closes them against accidental mutation and makes their behavior completely predictable. Set once in constructor, never changed.

### Why `List<Instruction>` is the program, not a root node
A ZARA program is a flat sequence of statements. There is no concept of a root or container instruction. Wrapping the list in a `ProgramNode` class would add an abstraction layer with no purpose.

### Why `HashMap` inside `Environment`
Variable lookup and assignment happen on every expression evaluation. O(1) average-case for both operations is the right choice. There is no ordering requirement that would justify `LinkedHashMap` or `TreeMap`.

### Why interfaces over abstract base classes
`NumberNode`, `StringNode`, `VariableNode`, `BinaryOpNode` share no implementation — only a contract. An abstract base class with no shared behavior is noise. The interface captures exactly what is shared.

### Why `consume(TokenType)` in the Parser
`advance()` blindly moves forward regardless of token type. `consume(TokenType)` asserts the type first. When a grammar rule says "a `when` block must be followed by a condition, then a colon, then a newline" — using `consume(COLON)` documents that contract and gives a clear error if violated.

### Why formatting logic is isolated in PrintInstruction
The `format()` private method separates "what to output" from "how to display it." If formatting rules ever change (e.g., displaying `16.00` instead of `16`), only `format()` needs updating — `execute()` stays untouched. Single Responsibility in action.

---

## 8. Error Handling Strategy (Fail-Fast Approach)

A core tenet of this interpreter is to **fail fast**. The architecture refuses to fail silently or propagate `null` values downstream. Illegal states trigger a `RuntimeException` immediately with clear, localized messages.

### Two categories of error

**Parse & Lexical errors** — source code is structurally invalid (e.g., missing colon after `when`, unterminated strings). These carry the line number where execution failed via `ParseException`.

**Runtime errors** — valid syntax but illegal at execution time (undefined variable, type mismatch, division by zero).

### Where errors originate

- `Environment.get()` — undefined variable access throws explicitly
- `BinaryOpNode.evaluate()` — type mismatch or unknown operator throws explicitly
- `Parser.consume()` — token type mismatch during parsing
- `Tokenizer.readString()` — unterminated strings
- Instruction constructors — null/empty input validation (fail-fast)

### Where errors are handled

One place: `Main.main()`. A top-level try/catch prints the error message and exits cleanly. This means no other class needs catch blocks — errors propagate up naturally. Scattering catch blocks throughout the codebase makes it impossible to reason about error flow.

### Custom exception: `ParseException`

`ParseException extends RuntimeException` accepts a message and a line number. The message it produces is immediately actionable: `"Line 4: expected ':' after condition"`.

---

## 9. Extension Points

The design is intentionally open for extension without modification of existing classes:

| Feature | Extension approach |
|---------|-------------------|
| `else` block | Add an optional `elseBody` field to `IfInstruction`. Parser reads it if the token after the `when` block is an `else` keyword. No changes to any other class. |
| `while` loop | New `WhileInstruction implements Instruction`. New `TokenType.WHILE`. Parser dispatches to a new `parseWhile()` method. Zero changes to existing instruction classes. |
| Modulo operator | Add `TokenType.PERCENT`. Handle `%` in `Tokenizer.readOperator()` and `BinaryOpNode.evaluate()`. Two additions in two places. |
| String length | New `LengthNode implements Expression`, or add a `length(x)` syntax handled in `parsePrimary()`. |
| Nested blocks | **Already works.** `IfInstruction.body` is `List<Instruction>` — it can hold any instruction including another `IfInstruction` or `RepeatInstruction`. |
| Better error messages | **Already designed for** — every `Token` carries a line number, and `ParseException` carries line information. Improving messages means improving the strings, not restructuring. |

---

## 10. What NOT To Do

**❌ Don't put evaluation logic inside the Parser.**
The Parser builds the tree. The tree evaluates itself. If the Parser calls `env.set()` or does arithmetic, the stages are not separated and the design has failed.

**❌ Don't use string comparisons to identify tokens.**
`if (token.getValue().equals("set"))` breaks the purpose of the `TokenType` enum. Use `token.getType() == TokenType.SET`.

**❌ Don't store `Environment` inside expression nodes.**
`Expression.evaluate(env)` receives the environment as a parameter — it is not the node's possession. Storing `env` in a node conflates syntax (tree structure) with runtime state.

**❌ Don't make `Token` mutable.**
A token is created once and read many times. Mutability here has no purpose and opens the door to bugs.

**❌ Don't check `instanceof` in the executor loop.**
```java
// This defeats the purpose of the Instruction interface:
if (instr instanceof AssignInstruction) { ... }
else if (instr instanceof PrintInstruction) { ... }
```
Call `instr.execute(env)` and let polymorphism route the call. The interface exists precisely so the executor does not need to know what kind of instruction it holds.

**❌ Don't build the whole system at once.**
The build order followed by the team:
1. `Token` + `TokenType`
2. `Tokenizer` — tested in isolation on simple inputs
3. `NumberNode`, `StringNode`, `VariableNode` — `evaluate()` tested directly
4. `Environment` — `set`/`get` tested including undefined variable error
5. `BinaryOpNode` — arithmetic and comparison tested
6. `AssignInstruction` + `PrintInstruction` — tested with a real `Environment`
7. `Parser` — started with just assignment and print, no blocks yet
8. `IfInstruction` + `BlockInstruction` + block parsing in Parser
9. `RepeatInstruction` + block parsing in Parser
10. `Interpreter` + `Main` — wired together, ran sample programs end-to-end

Each step was working and tested before the next began.

---

*This document defines the architecture, contracts, and reasoning behind the ZARA interpreter design. The decisions recorded here constrain and guide the implementation — they do not replace it.*
