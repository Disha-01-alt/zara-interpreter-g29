# ZARA Interpreter — Advanced OOP Project

> **Zero-ceremony Arithmetic and Reasoning Assembler**
> A fully functional interpreter for the ZARA mini scripting language, built in pure Java.

**Course:** Advanced Object-Oriented Programming in Java
**University:** Sitare University
**Team:** Group 29

---

## What is ZARA?

ZARA is a clean, minimal scripting language — like a simpler version of Python. The interpreter reads `.zara` source files, parses them into an internal tree structure, and executes them to produce output.

```
set x = 10
set y = 3
set result = x + y * 2
show result
```
**Output:** `16`

---

## How the Interpreter Works

The interpreter is a three-stage pipeline:

```
Source Code (.zara)  →  Tokenizer  →  Parser  →  Executor  →  Output
                        (Stage 1)     (Stage 2)   (Stage 3)
```

| Stage | Component | Input | Output |
|-------|-----------|-------|--------|
| 1 | **Tokenizer** | Raw source string | `List<Token>` |
| 2 | **Parser** | Token list | `List<Instruction>` (AST) |
| 3 | **Executor** | Instruction list + Environment | Program output |

---

## ZARA Language Reference

| Feature | Syntax | Example |
|---------|--------|---------|
| Assignment | `set <var> = <expr>` | `set x = 10` |
| Print | `show <expr>` | `show x` |
| Conditional | `when <cond>:` | `when x > 5:` |
| Loop | `loop <n>:` | `loop 3:` |
| Arithmetic | `+ - * /` | `x + y * 2` |
| Comparison | `> < ==` | `score > 50` |
| Strings | `"text"` | `"Hello from ZARA"` |

---

## Project Structure

```
zara-interpreter-g29/
│
├── src/main/java/zara/
│   │
│   ├── Main.java                     # CLI entry point
│   │
│   ├── ast/                          # Expression tree nodes
│   │   ├── Expression.java           # Interface — evaluate(Environment)
│   │   ├── NumberNode.java           # Literal number (42, 3.14)
│   │   ├── StringNode.java           # Literal string ("hello")
│   │   ├── VariableNode.java         # Variable reference (x, score)
│   │   └── BinaryOpNode.java         # Binary operation (x + y, a > b)
│   │
│   ├── lexer/                        # Tokenization (Stage 1)
│   │   ├── TokenType.java            # Enum of all token types
│   │   ├── Token.java                # Immutable token data class
│   │   └── Tokenizer.java            # Source code → List<Token>
│   │
│   ├── parser/                       # Parsing (Stage 2)
│   │   ├── Parser.java               # List<Token> → List<Instruction>
│   │   └── ParseException.java       # Parser-specific error type
│   │
│   ├── instruction/                  # Instruction execution (Stage 3)
│   │   ├── Instruction.java          # Interface — execute(Environment)
│   │   ├── AssignInstruction.java    # set x = <expr>
│   │   ├── PrintInstruction.java     # show <expr>
│   │   ├── IfInstruction.java        # when <cond>: <body>
│   │   ├── RepeatInstruction.java    # loop <n>: <body>
│   │   └── BlockInstruction.java     # Sequential instruction block
│   │
│   └── runtime/                      # Runtime components
│       ├── Environment.java          # Variable store (name → value)
│       └── Interpreter.java          # Connects all 3 stages
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
│       ├── instruction/
│       │   ├── AssignInstructionTest.java
│       │   ├── BlockInstructionTest.java
│       │   ├── IfInstructionTest.java
│       │   ├── PrintInstructionTest.java
│       │   └── RepeatInstructionTest.java
│       │
│       ├── lexer/
│       │   └── TokenizerTest.java
│       │
│       ├── parser/
│       │   └── ParserTest.java
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

---

## How to Build & Run

### Compile
```bash
javac -d out src/main/java/zara/*.java src/main/java/zara/ast/*.java src/main/java/zara/lexer/*.java src/main/java/zara/parser/*.java src/main/java/zara/instruction/*.java src/main/java/zara/runtime/*.java
```

### Run a ZARA program
```bash
java -cp out zara.Main test/program1.zara
```

### Expected Outputs

| Program | Description | Output |
|---------|------------|--------|
| program1.zara | Arithmetic & variables | `16` |
| program2.zara | String output | `Sitare` `Hello from ZARA` |
| program3.zara | Conditional | `Pass` |
| program4.zara | Loop | `1` `2` `3` `4` |

---

## Sample Programs

### Program 1 — Arithmetic & Variables
```
set x = 10
set y = 3
set result = x + y * 2
show result
```
Output: `16`

### Program 2 — String Output
```
set name = "Sitare"
show name
show "Hello from ZARA"
```
Output: `Sitare` and `Hello from ZARA`

### Program 3 — Conditional
```
set score = 85
when score > 50:
    show "Pass"
```
Output: `Pass`

### Program 4 — Loop
```
set i = 1
loop 4:
    show i
    set i = i + 1
```
Output: `1` `2` `3` `4`

---

## Class Responsibility Map

| Class | Package | Responsibility |
|-------|---------|---------------|
| Main | `zara` | CLI entry point |
| TokenType | `zara.lexer` | Enum of all token kinds |
| Token | `zara.lexer` | Holds one token's type, value, line |
| Tokenizer | `zara.lexer` | Source string → token list |
| Expression | `zara.ast` | Interface for evaluatable nodes |
| NumberNode | `zara.ast` | Literal number → Double |
| StringNode | `zara.ast` | Literal string → String |
| VariableNode | `zara.ast` | Variable lookup → env.get() |
| BinaryOpNode | `zara.ast` | Arithmetic/comparison operations |
| Parser | `zara.parser` | Tokens → instruction list (AST) |
| ParseException | `zara.parser` | Parser-specific error type |
| Instruction | `zara.instruction` | Interface for executable actions |
| AssignInstruction | `zara.instruction` | Variable assignment |
| PrintInstruction | `zara.instruction` | Output to console |
| IfInstruction | `zara.instruction` | Conditional execution |
| RepeatInstruction | `zara.instruction` | Fixed-count loop |
| BlockInstruction | `zara.instruction` | Sequential instruction block |
| Environment | `zara.runtime` | Variable store (Map) |
| Interpreter | `zara.runtime` | Orchestrates the 3-stage pipeline |

---

## Test Coverage

| Test File | Package | Coverage |
|-----------|---------|----------|
| TokenizerTest | `zara.lexer` | Lexer token generation |
| ExpressionEvalTest | `zara.ast` | AST node evaluation |
| ParserTest | `zara.parser` | Token list to instruction tree |
| AssignInstructionTest | `zara.instruction` | Store, overwrite, expression eval |
| PrintInstructionTest | `zara.instruction` | Integer, decimal, string, variable |
| IfInstructionTest | `zara.instruction` | True, false, multiple body |
| RepeatInstructionTest | `zara.instruction` | Count, zero, variable increment |
| BlockInstructionTest | `zara.instruction` | Sequential, empty, shared env |
| EnvironmentTest | `zara.runtime` | Store, overwrite, undefined error |
| InterpreterIntegrationTest | `zara.runtime` | Full pipeline end-to-end |

---

## Design Principles Followed

- **Single Responsibility** — Each class has one clearly defined job
- **Open/Closed** — New instructions can be added without modifying existing code
- **Liskov Substitution** — All instructions and expressions are interchangeable via their interfaces
- **Interface Segregation** — `Instruction` and `Expression` interfaces have single methods
- **Dependency Inversion** — Executor depends on abstractions, not concrete classes
- **Immutability** — All node and token fields are `private final`
- **Fail Fast** — Constructor-level input validation throughout

---

## Team

| Role | Member | Responsibility |
|------|--------|---------------|
| Member 1 | Disha Sahu | Code Design, Tokenizer (Lexer) |
| Member 2 | Satish Mahto | Parser & AST |
| Member 3 | Shivam Shukla | Instructions, Interpreter & Testing |
