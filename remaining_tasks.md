# ZARA Interpreter — Remaining Tasks for Team Members

> **Status:** All core code is **complete and working**. All 4 sample programs run correctly.
> These remaining tasks are for **polish, testing, and viva preparation**.

---

## For ALL Members

### 📖 Viva Preparation (CRITICAL)
- [ ] Read `code_explanation.txt` thoroughly — it explains every file and every line
- [ ] Understand ALL three stages (Tokenizer → Parser → Executor), not just your own
- [ ] Practice tracing through sample programs step by step
- [ ] Be able to answer: "What happens when I run `set result = x + y * 2`?"
- [ ] Be able to answer: "Where does operator precedence get handled?"
- [ ] Be able to answer: "What design patterns did you use?"

### 🧪 Run the Programs Locally
```bash
# Compile everything
javac -d out src/main/java/zara/*.java src/main/java/zara/ast/*.java src/main/java/zara/lexer/*.java src/main/java/zara/instruction/*.java src/main/java/zara/parser/*.java src/main/java/zara/runtime/*.java

# Run all 4 sample programs
java -cp out zara.Main samples/program1_arithmetic.zara    # Expected: 16
java -cp out zara.Main samples/program2_strings.zara       # Expected: Sitare  Hello from ZARA
java -cp out zara.Main samples/program3_conditional.zara   # Expected: Pass
java -cp out zara.Main samples/program4_loop.zara          # Expected: 1  2  3  4
```

---

## Stage 1 — Core Architecture, Lexer, Runtime & Testing

### Files & Directories
| File/Directory | What it does |
|------|-------------|
| **1. Lexer Component** | |
| `TokenType.java` | Enum of all token types (Keywords, Operators, etc) |
| `Token.java` | Immutable token data class |
| `Tokenizer.java` | Complex engine: reads string → produces token list |
| **2. Runtime Engine** | |
| `Environment.java` | Variable storage and Runtime exception handling |
| `Interpreter.java` | Main loop connecting Lexer → Parser → Executor |
| `Main.java` | CLI entry point and file reader |
| **3. Testing & Samples** | |
| `test/java/zara/` | ALL 33 unit and integration tests across the project |
| `samples/` | All 4 official ZARA sample execution scripts |
| **4. Architecture** | |
| `README.md` | Core layout, project setup, and design tracking |

### ✅ Already Done (Massive Contribution)
- [x] **Repository Mgmt:** Initial codebase setup and package architecture.
- [x] **Lexer:** String parsing, number parsing, keyword detection without regex.
- [x] **Indentation Engine:** Built the `processIndentation` algorithm critical for loops/ifs.
- [x] **Runtime:** Built `Environment`, `Interpreter` pipeline, and `Main` entry point.
- [x] **Tests:** Wrote rigorous suites validating the entire AST, Parser, and Runtime limits.
- [x] **Documentation:** Built the final JavaDoc structures and `.zara` demos.

### 📋 Remaining Tasks
- [ ] **Test execution manually** — Run all your sample programs one last time:
  ```bash
  java -cp out zara.Main samples/program4_loop.zara
  ```
- [ ] **Review PRs** — Ensure GitHub Insights shows all your merged commits.
- [ ] **Viva-specific**: Since you integrated everything, be ready to explain:
  - How `Interpreter.java` connects the output of Tokenizer to Parser.
  - The logic behind `Environment.java`'s error throwing.
  - How the Tokenizer tracks INDENT/DEDENT so the Parser knows about blocks.
  - Everything listed about your components in `code_explanation.txt`.

---

## Stage 2 — Parser & AST

### Files & Responsibilities
| File | What it does |
|------|-------------|
| `Expression.java` | Interface: evaluate(env) → Object |
| `NumberNode.java` | Literal number node |
| `StringNode.java` | Literal string node |
| `VariableNode.java` | Variable lookup node |
| `BinaryOpNode.java` | Binary operation (+, -, *, /, >, <, ==) |
| `ParseException.java` | Custom error with line number |
| `Parser.java` | Tokens → instruction list |

### ✅ Already Done
- [x] All four AST node types (Number, String, Variable, BinaryOp)
- [x] Three-level precedence chain (parseExpression → parseTerm → parsePrimary)
- [x] All four statement parsers (assignment, print, conditional, loop)
- [x] Block parsing using INDENT/DEDENT tokens
- [x] Parenthesized expression support
- [x] String concatenation and equality in BinaryOpNode
- [x] Division by zero protection
- [x] ParseException with line numbers

### 📋 Remaining Tasks
- [ ] **Run ExpressionEvalTest.java** — 18 tests covering all AST nodes
- [ ] **Run ParserTest.java** — 21 tests covering parsing + execution
- [ ] **Test edge cases manually**:
  - Nested blocks: `when` inside `loop` inside `when`
  - Missing colon after `when` (should give clear error)
  - Missing `=` in assignment (should give clear error)
  - Complex expression: `set r = 1 + 2 * 3 - 4 / 2`
- [ ] **Viva-specific**: Be ready to explain:
  - The three-level precedence chain and WHY it gives correct results
  - Walk through `x + y * 2` parsing step by step
  - Why Expression.evaluate() returns Object (not Double)
  - Why BinaryOpNode holds Expression references (Composite Pattern)
  - What happens if you call parsePrimary on an unexpected token

---

## Stage 3 — Instructions, Interpreter & Testing

### Files & Responsibilities
| File | What it does |
|------|-------------|
| `Instruction.java` | Interface: execute(env) → void |
| `AssignInstruction.java` | set x = \<expr\> |
| `PrintInstruction.java` | show \<expr\> |
| `IfInstruction.java` | when \<cond\>: \<body\> |
| `RepeatInstruction.java` | loop \<n\>: \<body\> |
| `BlockInstruction.java` | Sequential instruction group |
| `Environment.java` | Variable store (name → value) |
| `Interpreter.java` | 3-stage pipeline orchestrator |
| `Main.java` | CLI entry point |

### ✅ Already Done
- [x] All instruction types implemented
- [x] Environment with get/set and undefined variable error
- [x] PrintInstruction formats whole numbers without ".0"
- [x] Interpreter connects all three stages with error handling
- [x] Main reads .zara files from command line

### 📋 Remaining Tasks
- [ ] **Run ALL test files** and verify they pass:
  - `AssignInstructionTest.java` (3 tests)
  - `PrintInstructionTest.java` (4 tests)
  - `IfInstructionTest.java` (3 tests)
  - `RepeatInstructionTest.java` (3 tests)
  - `BlockInstructionTest.java` (3 tests)
  - `EnvironmentTest.java` (5 tests)
  - `InterpreterIntegrationTest.java` (7 tests — end-to-end)
- [ ] **Test error handling manually**:
  - Use an undefined variable: `show x` (should print error message)
  - Divide by zero: `set x = 10 / 0` (should print error message)
  - Wrong file path: `java zara.Main nonexistent.zara` (should handle gracefully)
- [ ] **Viva-specific**: Be ready to explain:
  - Why Instruction.execute() returns void (effects, not values)
  - How PrintInstruction decides between "16" and "3.14"
  - Why Environment is not a singleton
  - How the top-level try/catch in Interpreter.run() works
  - Why the executor loop doesn't use instanceof

---

## 🏁 Optional Extensions (Bonus)

If you have time, pick ONE to implement:

| Extension | Difficulty | What to change |
|-----------|-----------|----------------|
| **Else block** | Easy | Add optional `elseBody` to IfInstruction. Parser reads `else:` keyword. |
| **While loop** | Medium | New WhileInstruction. New WHILE TokenType. New parseWhile() in Parser. |
| **String length** | Easy | New LengthNode or `length(x)` syntax in parsePrimary(). |
| **Equality check** | ✅ Done | Already implemented — `==` works for numbers and strings. |
| **Helpful errors** | ✅ Done | ParseException carries line numbers. All errors are clear. |
| **Nested blocks** | ✅ Done | Already works — List<Instruction> body handles any depth. |

---

## 📅 Timeline Suggestion

| When | What to do |
|------|-----------|
| **Today** | Each member runs tests for their own component |
| **Tomorrow** | Run all 4 sample programs together, fix any issues |
| **Day before viva** | Read `code_explanation.txt` and practice Q&A together |
| **Viva day** | Each member should be able to explain ANY part of the code |

---

> ⚠️ **IMPORTANT**: The viva will ask ANY member about ANY part of the code.
> Don't just know your own section — understand how all three stages connect.
> The `code_explanation.txt` file explains everything in detail.
