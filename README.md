# ZARA Interpreter — Full System Design Document

**Project:** Advanced OOP in Java — Mini Scripting Engine  
**Language Assigned:** ZARA (Zero-ceremony Arithmetic and Reasoning Assembler)  
**Course:** Advanced Object-Oriented Programming | Sitare University  
**Team Size:** 3 members | 12.5 marks

---

## Table of Contents

1. [What We Are Building](#1-what-we-are-building)
2. [High-Level Pipeline](#2-high-level-pipeline)
3. [Directory & File Structure](#3-directory--file-structure)
4. [ZARA Language Specification](#4-zara-language-specification)
5. [TokenType Enum Design](#5-tokentype-enum-design)
6. [Class-by-Class Design](#6-class-by-class-design)
   - 6.1 Token
   - 6.2 Tokenizer
   - 6.3 Expression Interface & Nodes
   - 6.4 Environment
   - 6.5 Instruction Interface & Classes
   - 6.6 Parser
   - 6.7 Interpreter
7. [Expression Tree — How Operator Precedence Works](#7-expression-tree--how-operator-precedence-works)
8. [Parser Call Chain — Operator Precedence by Design](#8-parser-call-chain--operator-precedence-by-design)
9. [Execution Flow — Step by Step](#9-execution-flow--step-by-step)
10. [Sample Program Walkthrough](#10-sample-program-walkthrough)
11. [Team Division of Work](#11-team-division-of-work)
12. [Build & Run Instructions](#12-build--run-instructions)
13. [Test Programs & Expected Outputs](#13-test-programs--expected-outputs)
14. [Extension Options](#14-extension-options)
15. [Viva Preparation](#15-viva-preparation)

---

## 1. What We Are Building

A working interpreter for the **ZARA** scripting language — written entirely in pure Java. When complete, a user can write a `.zara` source file and run it through our program to see real output on screen.

The interpreter is a **3-stage pipeline**:

```
Source Code (.zara file)
        │
        ▼
  ┌─────────────┐
  │  Tokenizer  │  → breaks source into labelled tokens
  └─────────────┘
        │  List<Token>
        ▼
  ┌─────────────┐
  │   Parser    │  → builds a tree of Instruction objects
  └─────────────┘
        │  List<Instruction>
        ▼
  ┌─────────────┐
  │  Evaluator  │  → executes each instruction using Environment
  └─────────────┘
        │
        ▼
     Output (stdout)
```

---

## 2. High-Level Pipeline

```
┌──────────────────────────────────────────────────────────────────┐
│                        Interpreter.run()                         │
│                                                                  │
│  sourceCode ──► Tokenizer ──► List<Token>                        │
│                                    │                             │
│                               Parser ──► List<Instruction>       │
│                                                │                 │
│                              Environment  ◄────┤                 │
│                                    │           │                 │
│                              execute() ◄───────┘                 │
│                                    │                             │
│                                 stdout                           │
└──────────────────────────────────────────────────────────────────┘
```

**Key insight:** The `Environment` object is created once and shared across ALL instruction executions. This is how variables set in one line are visible to the next.

---

## 3. Directory & File Structure

```
ZARAInterpreter/
│
├── src/
│   └── main/
│       └── java/
│           └── zara/
│               │
│               ├── Main.java                   ← CLI entry point
│               ├── Interpreter.java            ← orchestrates all 3 stages
│               │
│               ├── tokenizer/
│               │   ├── TokenType.java          ← enum of all token kinds
│               │   ├── Token.java              ← immutable token object
│               │   └── Tokenizer.java          ← source → List<Token>
│               │
│               ├── parser/
│               │   └── Parser.java             ← List<Token> → List<Instruction>
│               │
│               ├── expression/
│               │   ├── Expression.java         ← interface
│               │   ├── NumberNode.java
│               │   ├── StringNode.java
│               │   ├── VariableNode.java
│               │   └── BinaryOpNode.java
│               │
│               ├── instruction/
│               │   ├── Instruction.java        ← interface
│               │   ├── AssignInstruction.java
│               │   ├── PrintInstruction.java
│               │   ├── IfInstruction.java
│               │   └── RepeatInstruction.java
│               │
│               └── runtime/
│                   └── Environment.java        ← variable store (Map)
│
├── programs/                                   ← .zara test files go here
│   ├── program1_arithmetic.zara
│   ├── program2_strings.zara
│   ├── program3_conditional.zara
│   └── program4_loop.zara
│
└── README.md
```

> **Why sub-packages?**  
> Grouping by responsibility (`tokenizer`, `parser`, `expression`, `instruction`, `runtime`) makes it immediately clear which team member owns which files, and prevents the "one big folder" mess that hides structure.

---

## 4. ZARA Language Specification

ZARA has exactly **4 statement types**. Every program is a sequence of these.

| Statement | Syntax | What it does |
|---|---|---|
| Assignment | `set <name> = <expr>` | Evaluates `<expr>` and stores result in a variable |
| Print | `show <expr>` | Evaluates `<expr>` and prints to stdout |
| Conditional | `when <expr>:` + indented body | Executes body only if condition is true |
| Loop | `loop <number>:` + indented body | Executes body exactly N times |

**Expressions** support:
- Literal numbers: `10`, `3.14`
- Literal strings: `"hello"`, `"Sitare"`
- Variable references: `x`, `result`, `score`
- Binary arithmetic: `+`, `-`, `*`, `/`
- Binary comparisons: `>`, `<`
- Operator precedence: `*` and `/` bind tighter than `+` and `-`

**Block bodies** (for `when` and `loop`) are lines indented with spaces or tabs after the header line ending in `:`.

---

## 5. TokenType Enum Design

```
TokenType enum
│
├── KEYWORDS
│   ├── SET        → "set"
│   ├── SHOW       → "show"
│   ├── WHEN       → "when"
│   └── LOOP       → "loop"
│
├── OPERATORS
│   ├── PLUS       → "+"
│   ├── MINUS      → "-"
│   ├── STAR       → "*"
│   ├── SLASH      → "/"
│   ├── EQUALS     → "="
│   ├── GT         → ">"
│   ├── LT         → "<"
│   └── COLON      → ":"
│
├── VALUES
│   ├── NUMBER     → e.g. 42, 3.14
│   ├── STRING     → e.g. "hello"
│   └── IDENTIFIER → e.g. x, score, result
│
└── STRUCTURE
    ├── NEWLINE    → end of line
    └── EOF        → end of file
```

---

## 6. Class-by-Class Design

---

### 6.1 Token

**File:** `tokenizer/Token.java`  
**Purpose:** An immutable snapshot of one piece of source code.

```
Token
├── TokenType type      ← what kind of token is this?
├── String value        ← the raw text from source (e.g. "set", "42", "x")
└── int line            ← which line it came from (useful for error messages)

Rules:
- All fields set in constructor, never changed
- Only getters — no setters
- Immutable by design
```

---

### 6.2 Tokenizer

**File:** `tokenizer/Tokenizer.java`  
**Purpose:** Walk the source string character by character and emit a `List<Token>`.

```
Tokenizer
├── String source       ← the full .zara source code
└── int pos             ← current character position

tokenize() algorithm:
  while pos < source.length():
    ch = source[pos]

    if ch is whitespace (not newline):
        skip it

    else if ch is '\n':
        emit NEWLINE token
        advance pos

    else if ch is a letter:
        read ahead until non-letter/non-digit
        word = collected chars
        if word in {"set","show","when","loop"}:
            emit keyword token
        else:
            emit IDENTIFIER token

    else if ch is a digit:
        read ahead until non-digit (allow one '.')
        emit NUMBER token

    else if ch == '"':
        read until closing '"'
        emit STRING token (without the quotes)

    else if ch is an operator (+,-,*,/,=,>,<,:):
        emit matching operator token

    advance pos

  emit EOF token
  return token list
```

**Watch out for:** handling multi-character numbers like `3.14`, strings with spaces inside them, and skipping blank lines gracefully.

---

### 6.3 Expression Interface & Nodes

**File:** `expression/Expression.java` and four node files  
**Purpose:** Represent any value-producing part of a program as an object in a tree.

```
«interface» Expression
└── Object evaluate(Environment env)
    Returns: Double (for numbers) or String (for text)

Implementations:

NumberNode
├── double value
└── evaluate() → return value (as Double)

StringNode
├── String value
└── evaluate() → return value (as String)

VariableNode
├── String name
└── evaluate() → return env.get(name)
                 throws RuntimeException if not defined

BinaryOpNode
├── Expression left
├── String operator     ← "+", "-", "*", "/", ">", "<"
├── Expression right
└── evaluate():
      leftVal  = left.evaluate(env)   ← always a Double for arithmetic
      rightVal = right.evaluate(env)
      switch operator:
        "+" → return (Double)leftVal + (Double)rightVal
        "-" → return (Double)leftVal - (Double)rightVal
        "*" → return (Double)leftVal * (Double)rightVal
        "/" → return (Double)leftVal / (Double)rightVal
        ">" → return (Double)leftVal > (Double)rightVal   ← returns Boolean
        "<" → return (Double)leftVal < (Double)rightVal   ← returns Boolean
```

**Key design choice:** `evaluate()` returns `Object` (not `double`). This is how a single method can return either a `Double` or a `String` or a `Boolean` depending on context. Java's polymorphism handles the rest.

---

### 6.4 Environment

**File:** `runtime/Environment.java`  
**Purpose:** The variable store. A simple `Map<String, Object>` shared across all instruction executions.

```
Environment
└── Map<String, Object> store   ← variable name → current value

set(String name, Object value)
    store.put(name, value)

get(String name)
    if store.containsKey(name):
        return store.get(name)
    else:
        throw new RuntimeException("Variable not defined: " + name)
```

**Why Object?** Variables can hold either numbers (`Double`) or strings (`String`). Using `Object` as the value type lets the same map store both.

---

### 6.5 Instruction Interface & Classes

**File:** `instruction/Instruction.java` and four instruction files  
**Purpose:** Each instruction is one complete action — it reads from / writes to Environment and may produce output.

```
«interface» Instruction
└── void execute(Environment env)

AssignInstruction
├── String variableName
├── Expression valueExpr
└── execute():
      Object result = valueExpr.evaluate(env)
      env.set(variableName, result)

PrintInstruction
├── Expression expr
└── execute():
      Object result = expr.evaluate(env)
      System.out.println(result)      ← handles both Double and String naturally

IfInstruction
├── Expression condition
├── List<Instruction> body
└── execute():
      Object result = condition.evaluate(env)
      if result equals Boolean.TRUE:
          for each instruction in body:
              instruction.execute(env)

RepeatInstruction
├── int count
├── List<Instruction> body
└── execute():
      for i from 1 to count (inclusive):
          for each instruction in body:
              instruction.execute(env)
```

**Note on printing numbers:** `System.out.println(16.0)` prints `16.0`, not `16`. You may want to format it: if result is a Double and has no fractional part, print as an integer. Example:

```java
if (result instanceof Double d) {
    if (d == Math.floor(d)) System.out.println((long)(double)d);
    else System.out.println(d);
} else {
    System.out.println(result);
}
```

---

### 6.6 Parser

**File:** `parser/Parser.java`  
**Purpose:** Consume the `List<Token>` and produce a `List<Instruction>`. This is the most complex class.

```
Parser
├── List<Token> tokens
└── int current           ← index of the token we are looking at now

Helper methods:
  peek()         → tokens.get(current)         (look without consuming)
  advance()      → tokens.get(current++)        (consume and return)
  check(type)    → peek().getType() == type
  match(type)    → if check(type): advance(); return true  else false
  skipNewlines() → while check(NEWLINE): advance()

parse() main loop:
  List<Instruction> instructions = new ArrayList<>()
  while not EOF:
      skipNewlines()
      if peek is SET:   instructions.add(parseAssign())
      if peek is SHOW:  instructions.add(parsePrint())
      if peek is WHEN:  instructions.add(parseIf())
      if peek is LOOP:  instructions.add(parseRepeat())
  return instructions

parseAssign():
  consume SET
  name = consume IDENTIFIER → .getValue()
  consume EQUALS
  expr = parseExpression()
  return new AssignInstruction(name, expr)

parsePrint():
  consume SHOW
  expr = parseExpression()
  return new PrintInstruction(expr)

parseIf():
  consume WHEN
  condition = parseExpression()
  consume COLON
  consume NEWLINE
  body = parseBody()
  return new IfInstruction(condition, body)

parseRepeat():
  consume LOOP
  count = consume NUMBER → Integer.parseInt(.getValue())
  consume COLON
  consume NEWLINE
  body = parseBody()
  return new RepeatInstruction(count, body)

parseBody():
  body = new ArrayList<>()
  while peek is INDENT (line starts with spaces):
      skipIndent()
      if peek is SET:   body.add(parseAssign())
      if peek is SHOW:  body.add(parsePrint())
      ...
      consume NEWLINE
  return body

── Expression parsing (operator precedence chain) ──────────────────

parseExpression()         handles + and -
  left = parseTerm()
  while peek is PLUS or MINUS:
      op = advance().getValue()
      right = parseTerm()
      left = new BinaryOpNode(left, op, right)
  
  ── comparison (optional extension in same method)
  if peek is GT or LT:
      op = advance().getValue()
      right = parseTerm()
      left = new BinaryOpNode(left, op, right)
  
  return left

parseTerm()               handles * and /
  left = parsePrimary()
  while peek is STAR or SLASH:
      op = advance().getValue()
      right = parsePrimary()
      left = new BinaryOpNode(left, op, right)
  return left

parsePrimary()            handles a single value
  if peek is NUMBER:   return new NumberNode(Double.parseDouble(advance().getValue()))
  if peek is STRING:   return new StringNode(advance().getValue())
  if peek is IDENTIFIER: return new VariableNode(advance().getValue())
  throw RuntimeException("Unexpected token: " + peek())
```

---

### 6.7 Interpreter

**File:** `Interpreter.java`  
**Purpose:** Wire all three stages together.

```
Interpreter

run(String sourceCode):
  1. Tokenizer tokenizer = new Tokenizer(sourceCode)
     List<Token> tokens = tokenizer.tokenize()

  2. Parser parser = new Parser(tokens)
     List<Instruction> instructions = parser.parse()

  3. Environment env = new Environment()
     for each instruction in instructions:
         instruction.execute(env)

Main.java entry point:
  args[0] = path to .zara file
  source = Files.readString(Path.of(args[0]))
  new Interpreter().run(source)
```

---

## 7. Expression Tree — How Operator Precedence Works

For the expression `x + y * 2`:

```
The tree shape enforces evaluation order automatically.
Deeper nodes evaluate first.

          BinaryOpNode [+]
         /              \
   VariableNode      BinaryOpNode [*]
       "x"           /            \
               VariableNode    NumberNode
                   "y"            2.0

Evaluation order:
  1. BinaryOpNode[*] evaluates → looks up y (=3), multiplies by 2 → 6.0
  2. BinaryOpNode[+] evaluates → looks up x (=10), adds 6.0 → 16.0
  3. AssignInstruction stores 16.0 into "result"
```

**Why does the tree have this shape?**  
Because `parseTerm()` (which handles `*`) is called inside `parseExpression()` (which handles `+`). The `*` node gets built at a deeper level in the call stack — so it sits lower (deeper) in the tree — and therefore evaluates first.

---

## 8. Parser Call Chain — Operator Precedence by Design

```
parseExpression()
    │
    ├── calls parseTerm() for the left side
    │       │
    │       └── calls parsePrimary() for its left side
    │               └── returns NumberNode / VariableNode / StringNode
    │
    ├── if it sees + or -, calls parseTerm() again for the right side
    │
    └── wraps both sides in BinaryOpNode(left, "+"/"-", right)

parseTerm()
    │
    ├── calls parsePrimary() for the left side
    ├── if it sees * or /, calls parsePrimary() again for the right side
    └── wraps in BinaryOpNode(left, "*"/"/", right)

Rule: lower in the call chain = higher operator precedence
  parsePrimary   → highest (atoms)
  parseTerm      → medium (* and /)
  parseExpression → lowest (+ and -)
```

---

## 9. Execution Flow — Step by Step

Full lifecycle for the program:
```
set x = 10
set y = 3
set result = x + y * 2
show result
```

```
STEP 1 — TOKENIZER
Input:  "set x = 10\nset y = 3\nset result = x + y * 2\nshow result\n"
Output: [SET, IDENTIFIER("x"), EQUALS, NUMBER("10"), NEWLINE,
         SET, IDENTIFIER("y"), EQUALS, NUMBER("3"), NEWLINE,
         SET, IDENTIFIER("result"), EQUALS,
             IDENTIFIER("x"), PLUS, IDENTIFIER("y"), STAR, NUMBER("2"),
         NEWLINE,
         SHOW, IDENTIFIER("result"), NEWLINE,
         EOF]

STEP 2 — PARSER
Reads SET → parseAssign() → AssignInstruction("x", NumberNode(10))
Reads SET → parseAssign() → AssignInstruction("y", NumberNode(3))
Reads SET → parseAssign() → AssignInstruction("result",
                BinaryOpNode(VariableNode("x"), "+",
                    BinaryOpNode(VariableNode("y"), "*", NumberNode(2))))
Reads SHOW → parsePrint() → PrintInstruction(VariableNode("result"))

STEP 3 — EVALUATOR
env = {}

AssignInstruction("x", NumberNode(10)).execute(env)
  → NumberNode(10).evaluate(env) = 10.0
  → env.set("x", 10.0)
  → env = {x: 10.0}

AssignInstruction("y", NumberNode(3)).execute(env)
  → env = {x: 10.0, y: 3.0}

AssignInstruction("result", BinaryOpNode(...)).execute(env)
  → BinaryOpNode("+").evaluate(env):
      left  = VariableNode("x").evaluate(env) = 10.0
      right = BinaryOpNode("*").evaluate(env):
                left  = VariableNode("y").evaluate(env) = 3.0
                right = NumberNode(2).evaluate(env)    = 2.0
                return 3.0 * 2.0 = 6.0
      return 10.0 + 6.0 = 16.0
  → env.set("result", 16.0)
  → env = {x: 10.0, y: 3.0, result: 16.0}

PrintInstruction(VariableNode("result")).execute(env)
  → VariableNode("result").evaluate(env) = 16.0
  → System.out.println(16)

OUTPUT: 16
```

---

## 10. Sample Program Walkthrough

### Program 3 — Conditional

**Source:**
```zara
set score = 85
when score > 50:
    show "Pass"
```

**Tokens:**
```
SET, IDENTIFIER("score"), EQUALS, NUMBER("85"), NEWLINE,
WHEN, IDENTIFIER("score"), GT, NUMBER("50"), COLON, NEWLINE,
IDENTIFIER("show") [indented — treated as body], STRING("Pass"), NEWLINE,
EOF
```

**Instruction tree:**
```
List<Instruction>:
  [0] AssignInstruction("score", NumberNode(85))
  [1] IfInstruction(
          condition: BinaryOpNode(VariableNode("score"), ">", NumberNode(50)),
          body: [PrintInstruction(StringNode("Pass"))]
      )
```

**Execution:**
```
env = {}
AssignInstruction → env = {score: 85.0}
IfInstruction:
  condition = BinaryOpNode(">").evaluate(env)
            = 85.0 > 50.0 = true
  body executes → PrintInstruction(StringNode("Pass"))
  → System.out.println("Pass")

OUTPUT: Pass
```

---

### Program 4 — Loop

**Source:**
```zara
set i = 1
loop 4:
    show i
    set i = i + 1
```

**Instruction tree:**
```
List<Instruction>:
  [0] AssignInstruction("i", NumberNode(1))
  [1] RepeatInstruction(
          count: 4,
          body: [
              PrintInstruction(VariableNode("i")),
              AssignInstruction("i", BinaryOpNode(VariableNode("i"), "+", NumberNode(1)))
          ]
      )
```

**Execution:**
```
env = {i: 1.0}
RepeatInstruction runs body 4 times:
  Iteration 1: print i (→ 1), set i = 1+1 = 2
  Iteration 2: print i (→ 2), set i = 2+1 = 3
  Iteration 3: print i (→ 3), set i = 3+1 = 4
  Iteration 4: print i (→ 4), set i = 4+1 = 5

OUTPUT:
1
2
3
4
```

---

## 11. Team Division of Work

```
Member 1 — Tokenizer Layer
  Owns:  TokenType.java, Token.java, Tokenizer.java
  Task:  Make the tokenizer correctly break all 4 sample programs into tokens.
  Test:  Print the token list to console and verify manually.

Member 2 — Expression & Runtime Layer
  Owns:  Expression.java, NumberNode.java, StringNode.java,
         VariableNode.java, BinaryOpNode.java, Environment.java
  Task:  Build and manually test expression trees by constructing them
         in Java code (before the Parser is ready).
  Test:  Hardcode a BinaryOpNode tree, call .evaluate() with a test Environment.

Member 3 — Parser & Instructions Layer
  Owns:  Parser.java, Instruction.java, AssignInstruction.java,
         PrintInstruction.java, IfInstruction.java, RepeatInstruction.java,
         Interpreter.java, Main.java
  Task:  Build the parser; wire all stages in Interpreter.run().
  Test:  Run Program 1 end-to-end first, then add Programs 2, 3, 4.

⚠️  All three members must understand all three parts for the viva.
    The examiner can ask anyone about any section.
```

---

## 12. Build & Run Instructions

### Compile

```bash
# From project root
javac -d out -sourcepath src/main/java src/main/java/zara/Main.java
```

Or with an IDE (IntelliJ / Eclipse): mark `src/main/java` as the sources root and run `Main.java`.

### Run a .zara program

```bash
java -cp out zara.Main programs/program1_arithmetic.zara
```

### Suggested build order (one small step at a time)

```
Step 1:  Implement Token + TokenType
Step 2:  Implement Tokenizer — test by printing token list
Step 3:  Implement NumberNode, StringNode, VariableNode, BinaryOpNode
Step 4:  Implement Environment
Step 5:  Implement AssignInstruction + PrintInstruction
Step 6:  Implement Parser (assignment + print only, no blocks yet)
Step 7:  Wire Interpreter — run Program 1 end-to-end ✓
Step 8:  Add IfInstruction + parseIf() in Parser — run Program 3 ✓
Step 9:  Add RepeatInstruction + parseRepeat() — run Program 4 ✓
Step 10: Run Program 2 (strings) — should work if Tokenizer handles quotes ✓
```

---

## 13. Test Programs & Expected Outputs

### Program 1 — Arithmetic
```zara
set x = 10
set y = 3
set result = x + y * 2
show result
```
**Expected:** `16`

### Program 2 — Strings
```zara
set name = "Sitare"
show name
show "Hello from ZARA"
```
**Expected:**
```
Sitare
Hello from ZARA
```

### Program 3 — Conditional
```zara
set score = 85
when score > 50:
    show "Pass"
```
**Expected:** `Pass`

### Program 4 — Loop
```zara
set i = 1
loop 4:
    show i
    set i = i + 1
```
**Expected:**
```
1
2
3
4
```

---

## 14. Extension Options

Each extension touches a specific, well-defined part of the system.

| Extension | Where to add it | What to change |
|---|---|---|
| Else block | `IfInstruction`, `Parser.parseIf()` | Add `List<Instruction> elseBody`; parse `else:` keyword |
| While loop | New `WhileInstruction`, `Parser` | Condition re-evaluated each iteration |
| String length | `BinaryOpNode` or new `LengthNode` | Built-in `len(x)` operation |
| Nested blocks | `Parser.parseBody()` | Recursively call `parseBody()` for deeper indent |
| Helpful errors | `Tokenizer`, `Parser`, `Environment` | Throw exceptions with `line` numbers from Token |
| Equality check | `TokenType`, `Tokenizer`, `BinaryOpNode` | Add `EQEQ (==)` token; handle in BinaryOpNode |

---

## 15. Viva Preparation

The examiner will ask each member individually. Every member must be able to answer all of these.

**Pipeline questions:**
- "Walk me through what happens step by step when your interpreter runs `set x = 10`."
- "What does the Tokenizer output for `x + y * 2`?"
- "What tree does the Parser build for `x + y * 2`? Draw it."

**Design questions:**
- "Why does `evaluate()` return `Object` instead of `double`?"
- "What is the Environment class for? What would break if you removed it?"
- "Where exactly does operator precedence get handled in your code?"
- "Why does `parseTerm()` call `parsePrimary()` and not `parseExpression()`?"

**Change questions:**
- "If you wanted to add a `>=` operator, what would you change?"
- "If you wanted to support nested `when` inside a `loop`, what would break?"
- "If two variables have the same name and are set twice, what happens?"

**Live code questions:**
- "I'll write a line on the board — tell me every step your program takes."
- "Show me in your code exactly where `x + y * 2` gets its precedence right."

---

*Document prepared for ZARA interpreter project — Sitare University Advanced OOP*
