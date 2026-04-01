# ZARA Interpreter — System Design Document

> **Language:** ZARA (Zero-ceremony Arithmetic and Reasoning Assembler)  
> **Course:** Advanced OOP in Java — Sitare University  
> **Document Type:** Engineering Design & Architecture (v2)

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
   - 5.7 [Interpreter Entry Point](#57-interpreter-entry-point)
6. [Design Principles Applied](#6-design-principles-applied)
7. [Key Design Decisions & Why](#7-key-design-decisions--why)
8. [Error Handling Strategy](#8-error-handling-strategy)
9. [Extension Points](#9-extension-points)
10. [What NOT To Do](#10-what-not-to-do)

---

## How to Read This Document

This is a **design document**, not an implementation guide. It describes:

- What each component is responsible for
- What contract (interface/signature) it exposes
- Why each design decision was made

It does **not** describe how to write the method bodies — that is the implementation work our team must do. Where code appears, it shows **structure and contracts only**, with bodies intentionally left empty. The reasoning sections explain the thinking; translating that thinking into working code is yours to do.

---

## 1. Philosophy & Design Goals

Before writing a single line of code, the team must agree on what kind of system this is. A scripting engine interpreter is not a data-processing app — it is a **pipeline of transformations**, where each stage produces a well-defined output that the next stage consumes. The design must reflect that.

### Core Engineering Goals

| Goal | What it means in practice |
|------|---------------------------|
| **High Cohesion** | Every class has one clear job. `Tokenizer` only tokenizes. `Environment` only stores variables. They do not bleed into each other. |
| **Loose Coupling** | Classes communicate through interfaces and contracts, not concrete implementations. The `Parser` does not know or care how tokens were produced. |
| **Open/Closed** | Adding a new instruction type (e.g., a `while` loop) should require adding a new class, not modifying existing ones. |
| **Liskov Substitution** | Any `Expression` can stand in for any other `Expression`. Any `Instruction` can stand in for any other `Instruction`. The executor loop never needs to know which concrete type it holds. |
| **Separation of Concerns** | Lexical analysis (Tokenizer), syntactic analysis (Parser), and semantic execution (Evaluator) are completely isolated layers. Bugs in one layer cannot contaminate others. |

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
│               │   ├── TokenType.java               # Enum — every kind of token ZARA can produce
│               │   ├── Token.java                   # Immutable value object — one piece of source
│               │   └── Tokenizer.java               # Reads source String → List<Token>
│               │
│               ├── ast/                             # Expression tree nodes (pure data, no I/O)
│               │   ├── Expression.java              # Interface: evaluate(Environment) → Object
│               │   ├── NumberNode.java              # Literal number
│               │   ├── StringNode.java              # Literal string
│               │   ├── VariableNode.java            # Variable reference
│               │   └── BinaryOpNode.java            # Two expressions joined by an operator
│               │
│               ├── runtime/                         # Runtime state
│               │   └── Environment.java             # Variable store: name → value
│               │
│               ├── instruction/                     # Executable instruction nodes
│               │   ├── Instruction.java             # Interface: execute(Environment) → void
│               │   ├── AssignInstruction.java       # set x = <expr>
│               │   ├── PrintInstruction.java        # show <expr>
│               │   ├── IfInstruction.java           # when <cond>: <block>
│               │   └── RepeatInstruction.java       # loop <n>: <block>
│               │
│               └── parser/                          # Stage 2: tokens → instructions
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
│               └── InterpreterIntegrationTest.java
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

The package structure mirrors the pipeline stages directly. When you open `zara/lexer/`, you are looking at tokenization code only. The `ast/` package is pure data — no I/O, no side effects. The `instruction/` package is pure behavior. This is not cosmetic; it enforces that every class has a clear home and a clear job before a single line is written.

---

## 4. The Pipeline: How Execution Flows

Understanding this end-to-end before writing any code is essential.

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

> **Convention used below:** Code blocks show contracts and field declarations only. Method bodies are left for the team to implement. The reasoning after each block is what matters — it explains the decisions that constrain what the implementation must do.

---

### 5.1 Token Layer

**Package:** `zara.lexer`

#### `TokenType.java` — What to include

An enum listing every distinct kind of token ZARA can produce. Every team member should be able to read this enum and immediately understand the full vocabulary of the language.

Minimum required entries:
- Literals: `NUMBER`, `STRING`, `IDENTIFIER`
- ZARA keywords: `SET`, `SHOW`, `WHEN`, `LOOP`
- Arithmetic operators: `PLUS`, `MINUS`, `STAR`, `SLASH`
- Comparison operators: `GREATER`, `LESS`
- Assignment: `ASSIGN` (the `=` sign)
- Structure: `COLON`, `NEWLINE`, `EOF`

**Why keywords get their own enum values (not just IDENTIFIER):**  
The Parser must distinguish `set` from a variable named `x`. If both were `IDENTIFIER`, the Parser would need to do `token.getValue().equals("set")` everywhere — fragile string comparison. With `TokenType.SET`, the check is `token.getType() == TokenType.SET` — type-safe and impossible to misspell silently.

#### `Token.java` — Contract

Fields required (all `private final`, set in constructor, no setters):
- `TokenType type` — what kind of token this is
- `String value` — the raw text from the source (e.g., `"set"`, `"42"`, `"result"`)
- `int line` — which line of source code this came from

Expose getters for all three fields. Implement `toString()` — you will thank yourself during debugging.

**Why `final` class:** Tokens are pure value objects — immutable data carriers. They should never be subclassed. A `Token` is completely defined by its three fields; there is no meaningful variation to express through inheritance.

**Why store line number:** Even if you don't use it for error messages in the base submission, it costs nothing and makes every future error message possible. A token without a line number is a debugging dead end.

---

### 5.2 Expression Tree (AST)

**Package:** `zara.ast`

Every value computation in ZARA — a number literal, a variable lookup, a calculation — is represented as a node object. All node classes implement the `Expression` interface.

#### `Expression.java` — Interface contract

```java
public interface Expression {
    Object evaluate(Environment env);
}
```

This single method is the entire contract. Every node in the expression tree must be able to evaluate itself given the current variable store, and return either a `Double` (for numbers) or a `String` (for text).

**Why `Object` return type and not generics:**  
You might consider `Expression<T>` with `T evaluate(Environment env)`. The problem: at `BinaryOpNode`, you don't know at compile time whether the left side produces a `Double` or a `String` — that depends on what the variable holds at runtime. Generics are a compile-time tool and cannot capture this runtime variability without becoming unwieldy wildcards. `Object` is the honest representation: the value is determined at runtime, not at compile time. The implementation of `BinaryOpNode` handles the distinction.

#### Node classes — Responsibilities

**`NumberNode`**
- Holds a single `double` value (set in constructor, no setter)
- `evaluate()` returns that stored value — it does not need to look at `env` at all

**`StringNode`**
- Holds a single `String` value (set in constructor, no setter)
- `evaluate()` returns that stored string — again, `env` is not needed

**`VariableNode`**
- Holds a variable name as a `String` (set in constructor, no setter)
- `evaluate()` asks the `Environment` for the current value of that name — it delegates entirely, containing no logic about what a variable *is*

**`BinaryOpNode`**
- Holds three things: a left `Expression`, an operator symbol (`String`), and a right `Expression` — all set in constructor, no setters
- `evaluate()` must: (1) evaluate the left child, (2) evaluate the right child, (3) apply the operator and return the result
- Arithmetic operators (`+`, `-`, `*`, `/`) return a `Double`
- Comparison operators (`>`, `<`) return a `Boolean`
- If either side is not a number when arithmetic is attempted, throw a `RuntimeException` with a clear message

**Why `BinaryOpNode` holds `Expression` references, not concrete node types:**  
This is what makes the tree composable. The right child of a `+` node can itself be a `*` node — which is exactly how `x + y * 2` gets the correct structure. The node doesn't care whether its children are literals, variables, or other operations. Any `Expression` works. This is the **Composite Pattern**.

---

### 5.3 Environment (Variable Store)

**Package:** `zara.runtime`

The `Environment` is a map from variable names to their current values. One instance is created per program run and shared across every instruction's execution.

#### Required interface

```java
public class Environment {
    // Internal: Map<String, Object> — name to value

    public void set(String name, Object value) { }

    public Object get(String name) { }
}
```

`set()` stores or updates a variable. `get()` retrieves the current value — if the variable has never been set, it must throw a `RuntimeException` with a message like `"Variable not defined: x"`. A raw `null` return would silently propagate errors far from their source.

**Why a dedicated class instead of passing a `Map` directly:**

A `Map<String, Object>` is a general container with no rules. `Environment` is a typed contract with enforced behavior: undefined variable access throws a meaningful error rather than returning `null`. If every class held a raw `Map`, every call site would need its own null check — and some would inevitably forget. Centralizing this logic in `Environment` means it happens in exactly one place.

**Why `Map<String, Object>` and not `Map<String, Double>`:**  
ZARA variables can hold either numbers or strings (`set name = "Sitare"` is valid). Using `Double` would misrepresent this. `Object` is correct — it acknowledges that the type of a variable's value is not known statically.

**Why `Environment` is not a singleton:**  
A singleton is global mutable state. It would make isolated testing impossible (instructions would mutate shared state between tests), would prevent running two programs concurrently, and would make adding scoped variables very difficult. Passing `env` as a parameter to `execute()` and `evaluate()` makes the dependency explicit and testable.

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

**Why `void`:** Instructions produce *effects*, not values. Assignment changes the `Environment`. Print writes to stdout. A conditional may or may not execute its body. None of these need to return anything — `void` is the accurate contract.

#### Instruction classes — Responsibilities

**`AssignInstruction`**  
Handles: `set x = 10 + 5`  
Holds: the variable name (`String`) and the expression to evaluate (`Expression`)  
Behavior: evaluate the expression, store the result in `env` under the variable name

**`PrintInstruction`**  
Handles: `show result` or `show "hello"`  
Holds: the expression to print (`Expression`)  
Behavior: evaluate the expression, print the result to standard output  
Note: a number stored as `16.0` should print as `16`, not `16.0` — ZARA users expect whole numbers to display as integers. This formatting concern belongs here and only here.

**`IfInstruction`**  
Handles: `when score > 50:` followed by an indented block  
Holds: the condition (`Expression`) and the body (`List<Instruction>`)  
Behavior: evaluate the condition; if it is `true`, execute every instruction in the body in order; otherwise do nothing

**`RepeatInstruction`**  
Handles: `loop 4:` followed by an indented block  
Holds: the count (`int`) and the body (`List<Instruction>`)  
Behavior: execute the full body `count` times in order

**Design note on `List<Instruction>` body fields:**  
Both `IfInstruction` and `RepeatInstruction` hold a list of child instructions. This list should be treated as immutable after construction — the instruction owns it from the moment it is created. Consider what happens if the list you receive in the constructor is still being modified by the Parser elsewhere.

**Why the body is `List<Instruction>` and not something else:**  
This means an `IfInstruction` body can contain another `IfInstruction` or a `RepeatInstruction`. Nesting works automatically — no special case required. The recursive structure of `List<Instruction>` handles arbitrary depth.

---

### 5.5 Tokenizer

**Package:** `zara.lexer`

The Tokenizer has one job: read the raw source `String` character by character and produce a `List<Token>`.

#### State it needs

- The source string (set once in constructor, never changed)
- A current position index (advances as characters are consumed)
- A current line counter (incremented on every newline character, used to populate `Token.line`)

#### What `tokenize()` must handle

| Input pattern | Output |
|---|---|
| Space or tab | Skip — do not emit a token |
| `\n` | Emit `NEWLINE` token, increment line counter |
| `"` | Read until the closing `"`, emit `STRING` token (value should be the content without quotes) |
| Digit | Read all consecutive digit/dot characters, emit `NUMBER` token |
| Letter or `_` | Read all consecutive letter/digit/underscore characters; check if it matches a keyword; emit the appropriate `TokenType` |
| `=` | Peek at next character — if `=`, emit `EQUALS` (`==`); otherwise emit `ASSIGN` (`=`) |
| `+`, `-`, `*`, `/`, `>`, `<`, `:` | Emit corresponding operator token |
| End of input | Emit one `EOF` token |

#### Key design decisions

**Keyword detection belongs in the identifier reader, not as a special first check.**  
Read the full word first, then check whether it is a keyword. Trying to detect keywords character by character leads to code that cannot distinguish `setter` from `set`.

**`pos` is the only mutable state that drives progress.**  
If the source is immutable and `pos` monotonically increases, the Tokenizer's behavior is entirely determined by those two things — easy to trace, easy to test. There should be no other hidden state.

**`line` is cheap to track and invaluable to have.**  
Every `\n` character increments it. The cost is one integer increment per newline. The benefit is that every token carries its line number, making error messages possible anywhere downstream.

---

### 5.6 Parser

**Package:** `zara.parser`

The Parser reads the `List<Token>` from the Tokenizer and builds a `List<Instruction>`. It is the most algorithmically complex component.

#### State it needs

- The full token list (set once in constructor)
- A current index into that list (advances as tokens are consumed)
- Two helper methods used everywhere:
  - `current()` — returns the token at the current index without advancing
  - `consume()` — returns the token at the current index and advances the index
  - `expect(TokenType)` — asserts the current token is the expected type, then consumes it; throws with a clear message if not

#### The `parse()` flow

`parse()` is a loop: while the current token is not `EOF`, look at the current token type and dispatch to the appropriate handler:

```
if current token is SET  → parseAssignment()   → produces AssignInstruction
if current token is SHOW → parsePrint()         → produces PrintInstruction
if current token is WHEN → parseConditional()   → produces IfInstruction
if current token is LOOP → parseLoop()          → produces RepeatInstruction
if current token is NEWLINE → skip it, advance
otherwise → throw ParseException (unexpected token)
```

Each handler consumes exactly the tokens it needs and returns one fully constructed instruction. `parse()` collects all returned instructions into the list it eventually returns.

#### The operator precedence problem and its solution

Consider `x + y * 2`. Parsed naively left-to-right: `(x + y) * 2 = 26`. Correct result: `x + (y * 2) = 16`. The Parser must produce a tree where `*` sits deeper than `+`.

**The solution is a three-level call chain:**

```
parseExpression()   handles: + − > <         (lowest precedence)
    └── calls parseTerm() to get each operand

parseTerm()         handles: * /              (higher precedence)
    └── calls parsePrimary() to get each operand

parsePrimary()      handles: a single value   (highest precedence / base case)
    returns: NumberNode, StringNode, or VariableNode
```

Why this works: `parseExpression` calls `parseTerm` to get its left side before it looks for a `+` or `-`. `parseTerm` eagerly consumes any `*` or `/` before returning. So by the time `parseExpression` sees its operator, the right side has already been bound tightly. The tree shape encodes precedence — no special flags or lookahead needed.

Walk through `x + y * 2` manually using this chain:
1. `parseExpression` calls `parseTerm` → which calls `parsePrimary` → returns `VariableNode("x")`
2. `parseTerm` sees no `*` or `/`, returns `VariableNode("x")` to `parseExpression`
3. `parseExpression` sees `+`, consumes it, calls `parseTerm` again for the right side
4. `parseTerm` calls `parsePrimary` → returns `VariableNode("y")`
5. `parseTerm` sees `*`, consumes it, calls `parsePrimary` → returns `NumberNode(2.0)`
6. `parseTerm` wraps: `BinaryOpNode("*", VariableNode("y"), NumberNode(2.0))` — returns this
7. `parseExpression` wraps: `BinaryOpNode("+", VariableNode("x"), BinaryOpNode("*", ...))`

Result: the `*` node sits deeper. Correct tree. Correct precedence. No special logic.

#### Parsing a `when` block (IfInstruction)

A `when` statement in ZARA looks like:
```
when score > 50:
    show "Pass"
show "done"
```

The parse sequence:
1. `expect(WHEN)` — consume the `when` keyword
2. Call `parseExpression()` — this returns the condition (`score > 50`)
3. `expect(COLON)` — consume the `:`
4. `expect(NEWLINE)` — consume the line ending
5. Parse the indented body — keep calling the top-level statement parser **while the current line is indented**; collect results into `List<Instruction>`
6. Construct and return `IfInstruction(condition, body)`

**How to detect indentation:** The Tokenizer emits `NEWLINE` tokens. After a `NEWLINE`, if the next token is also a `NEWLINE` or is a top-level keyword like `SET`/`SHOW`/`WHEN`/`LOOP` at column 0, the block has ended. The simplest practical approach: track whether the source line the current token came from starts with whitespace. If yes, the token is part of an indented block. Your Tokenizer already stores line numbers in every token — you can check the raw source to determine indentation for that line.

#### Parsing a `loop` block (RepeatInstruction)

A `loop` statement:
```
loop 4:
    show i
    set i = i + 1
```

The parse sequence:
1. `expect(LOOP)` — consume `loop`
2. `expect(NUMBER)` — consume the count; parse its value as an `int`
3. `expect(COLON)` — consume `:`
4. `expect(NEWLINE)` — consume the line ending
5. Parse the indented body using the same block-detection approach as `when`
6. Construct and return `RepeatInstruction(count, body)`

Note: `loop` takes a **literal number only** — not a variable or expression. The count is determined at parse time, not evaluation time. This is why `RepeatInstruction` stores an `int`, not an `Expression`.

---

### 5.7 Interpreter Entry Point

**Package:** `zara` (root)

```java
public class Interpreter {
    public void run(String sourceCode) {
        // Step 1: tokenize sourceCode → List<Token>
        // Step 2: parse token list → List<Instruction>
        // Step 3: create Environment, execute each instruction
    }
}
```

This class is a pipeline orchestrator. Its only job is to connect the three stages. If this class ever contains parsing logic, evaluation logic, or arithmetic — something has gone wrong with the separation of concerns.

`Main.java` reads the source file from the command-line argument, reads its contents into a `String`, and calls `new Interpreter().run(sourceCode)`.

---

## 6. Design Principles Applied

### Composite Pattern — Expression Tree

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

### Command Pattern — Instructions

Every instruction is an object that knows how to execute itself. The executor loop in `Interpreter.run()` does not know or care whether it is executing an assignment, a print, a conditional, or a loop. It calls `.execute(env)` and the object handles the rest. Adding a new instruction type requires no changes to the executor loop.

### Pipeline Pattern — Three Isolated Stages

`Tokenizer → Parser → Executor` is a strict pipeline. No stage holds a reference to any other stage. Each consumes a well-typed input and produces a well-typed output. This means each stage can be tested in complete isolation.

### Dependency Inversion — `Expression` and `Instruction` interfaces

High-level code (`Interpreter`, the executor loop) depends on the `Instruction` interface, not on `AssignInstruction` or `PrintInstruction`. This is why the executor loop is just a `for` loop calling `.execute()` — it is decoupled from every concrete instruction class.

---

## 7. Key Design Decisions & Why

### Why all Expression node classes should be `final`

Node classes represent fixed, well-defined values. `NumberNode` is a number. There is no meaningful subtype of "a number literal." Marking them `final` closes the class against accidental subclassing and makes their behavior completely predictable.

### Why `List<Instruction>` is the program, not a root node

A ZARA program is a flat sequence of statements. There is no concept of a root or container instruction. Wrapping the list in a `ProgramNode` class would add an abstraction layer with no purpose. The list is the program.

### Why `HashMap` inside `Environment`

Variable lookup and assignment happen on every expression evaluation. O(1) average-case for both operations is the right choice. There is no ordering requirement that would justify `LinkedHashMap` or `TreeMap`.

### Why interfaces over abstract base classes for `Expression` and `Instruction`

`NumberNode`, `StringNode`, `VariableNode`, `BinaryOpNode` share no implementation — only a contract. An abstract base class with no shared behavior is noise. The interface captures exactly what is shared (the `evaluate` method signature) without inventing fake shared state.

### Why `expect()` in the Parser

`consume()` blindly advances regardless of token type. `expect()` asserts the type first. When a grammar rule says "a `when` block must be followed by a condition, then a colon, then a newline" — using `expect(COLON)` documents that contract and gives a clear error if violated. Using `consume()` silently accepts whatever happens to be next, producing bugs far from their source.

---

## 8. Error Handling Strategy

### Two categories of error

**Parse errors** — source code is structurally invalid (e.g., missing colon after `when`, unrecognized character). Should carry the line number from the relevant token.

**Runtime errors** — valid syntax but illegal at execution time (e.g., accessing an undefined variable, applying arithmetic to a string).

### Where errors originate

- `Environment.get()` — undefined variable access
- `BinaryOpNode.evaluate()` — type mismatch (e.g., string where number expected)
- `Parser.expect()` — token type mismatch during parsing
- `Parser.parsePrimary()` — unexpected token where a value was expected

### Where errors are handled

One place: `Interpreter.run()`. A top-level try/catch there prints the error message and exits cleanly. This means no other class needs catch blocks — errors propagate up naturally. Scattering catch blocks throughout the codebase makes it impossible to reason about error flow.

### Custom exception for parse errors

Define a `ParseException extends RuntimeException` that accepts a message and a line number. The message it produces should be immediately actionable: `"Line 4: expected ':' after condition"` is useful. `"NullPointerException at Parser.java:83"` is not.

---

## 9. Extension Points

The design is intentionally open for extension without modification of existing classes:

| Feature | Extension approach |
|---------|-------------------|
| `else` block | Add an optional `elseBody` field to `IfInstruction`. Parser reads it if the token after the `when` block is an `else` keyword. No changes to any other class. |
| `while` loop | New `WhileInstruction implements Instruction`. New `TokenType.WHILE`. Parser dispatches to a new `parseWhile()` method. Zero changes to existing instruction classes. |
| `==` equality | Add `TokenType.EQUALS`. Handle the `"=="` case in `BinaryOpNode`. One additional case in one switch. |
| String length | New `LengthNode implements Expression`, or add a `length(x)` syntax handled in `parsePrimary()`. |
| Nested blocks | Already works. `IfInstruction.body` is `List<Instruction>` — it can hold any instruction including another `IfInstruction` or `RepeatInstruction`. The recursive structure handles arbitrary nesting. |
| Helpful error messages | Already designed for — every `Token` carries a line number, and `ParseException` carries line information. Improving messages means improving the strings passed to exceptions, not restructuring the code. |

---

## 10. What NOT To Do

**❌ Don't put evaluation logic inside the Parser.**  
The Parser builds the tree. The tree evaluates itself. If your Parser calls `env.set()` or does arithmetic, the stages are not separated and the design has failed.

**❌ Don't use string comparisons to identify tokens.**  
`if (token.getValue().equals("set"))` breaks the purpose of the `TokenType` enum. Use `token.getType() == TokenType.SET`.

**❌ Don't store `Environment` inside expression nodes.**  
`Expression.evaluate(env)` receives the environment as a parameter — it is not the node's possession. Storing `env` in a node at parse time conflates syntax (tree structure) with runtime state. These are different things.

**❌ Don't make `Token` mutable.**  
A token is created once and read many times. Mutability here has no purpose and opens the door to bugs where a downstream component changes a token that another component is still reading.

**❌ Don't check `instanceof` in the executor loop.**  
```java
// This defeats the purpose of the Instruction interface:
if (instr instanceof AssignInstruction) { ... }
else if (instr instanceof PrintInstruction) { ... }
```
Call `instr.execute(env)` and let polymorphism route the call. The interface exists precisely so the executor loop does not need to know what kind of instruction it holds.

**❌ Don't build the whole system at once.**  
Recommended build order:
1. `Token` + `TokenType`
2. `Tokenizer` — test it in isolation on simple inputs
3. `NumberNode`, `StringNode`, `VariableNode` — test `evaluate()` directly
4. `Environment` — test `set`/`get` including undefined variable error
5. `BinaryOpNode` — test arithmetic and comparison
6. `AssignInstruction` + `PrintInstruction` — test with a real `Environment`
7. `Parser` — start with just assignment and print, no blocks yet
8. `IfInstruction` + block parsing in Parser
9. `RepeatInstruction` + block parsing in Parser
10. `Interpreter` + `Main` — wire together, run the sample programs

Each step should be working and tested before the next begins.

---

*This document defines the architecture, contracts, and reasoning behind the ZARA interpreter design. Method bodies are the implementation work of the team. The decisions recorded here constrain and guide that work — they do not replace it.*
