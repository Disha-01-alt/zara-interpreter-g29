package zara.lexer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Tokenizer.
 *
 * Each test targets one specific behaviour so that when something breaks
 * you can immediately see which rule failed.
 *
 * How to run (plain javac — no build tool):
 * 1. Download junit-platform-console-standalone JAR
 * 2. javac -cp junit.jar -d out src/test/java/zara/lexer/TokenizerTest.java
 * 3. java -cp junit.jar:out org.junit.platform.console.ConsoleLauncher
 * --select-class=zara.lexer.TokenizerTest
 */
class TokenizerTest {

    // ── helpers ───────────────────────────────────────────────────────────────

    /** Tokenize source and strip the trailing EOF token for cleaner assertions. */
    private List<Token> lex(String source) {
        List<Token> tokens = new Tokenizer(source).tokenize();
        // last token is always EOF — remove it so tests can use simple indices
        assertFalse(tokens.isEmpty(), "tokenize() must return at least one token (EOF)");
        assertEquals(TokenType.EOF, tokens.get(tokens.size() - 1).getType(),
                "Last token must always be EOF");
        return tokens.subList(0, tokens.size() - 1);
    }

    private void assertToken(Token token, TokenType expectedType, String expectedValue) {
        assertEquals(expectedType, token.getType(),
                "Wrong type for token with value '" + token.getValue() + "'");
        assertEquals(expectedValue, token.getValue(),
                "Wrong value for token of type " + token.getType());
    }

    // ── Token class contract ──────────────────────────────────────────────────

    @Test
    void tokenStoresAllThreeFields() {
        Token t = new Token(TokenType.NUMBER, "42", 5);
        assertEquals(TokenType.NUMBER, t.getType());
        assertEquals("42", t.getValue());
        assertEquals(5, t.getLine());
    }

    @Test
    void tokenToStringIsReadable() {
        Token t = new Token(TokenType.IDENTIFIER, "result", 2);
        String s = t.toString();
        // must mention the type, value, and line somehow
        assertTrue(s.contains("IDENTIFIER"), "toString should include type");
        assertTrue(s.contains("result"), "toString should include value");
        assertTrue(s.contains("2"), "toString should include line number");
    }

    // ── EOF is always present ─────────────────────────────────────────────────

    @Test
    void emptySourceProducesOnlyEOF() {
        List<Token> tokens = new Tokenizer("").tokenize();
        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).getType());
    }

    @Test
    void eofIsAlwaysLastToken() {
        List<Token> tokens = new Tokenizer("set x = 1").tokenize();
        Token last = tokens.get(tokens.size() - 1);
        assertEquals(TokenType.EOF, last.getType());
    }

    // ── Keywords ──────────────────────────────────────────────────────────────

    @Test
    void setIsKeywordNotIdentifier() {
        List<Token> tokens = lex("set");
        assertEquals(1, tokens.size());
        assertToken(tokens.get(0), TokenType.SET, "set");
    }

    @Test
    void showIsKeywordNotIdentifier() {
        List<Token> tokens = lex("show");
        assertEquals(1, tokens.size());
        assertToken(tokens.get(0), TokenType.SHOW, "show");
    }

    @Test
    void whenIsKeywordNotIdentifier() {
        List<Token> tokens = lex("when");
        assertEquals(1, tokens.size());
        assertToken(tokens.get(0), TokenType.WHEN, "when");
    }

    @Test
    void loopIsKeywordNotIdentifier() {
        List<Token> tokens = lex("loop");
        assertEquals(1, tokens.size());
        assertToken(tokens.get(0), TokenType.LOOP, "loop");
    }

    @Test
    void wordThatStartsWithKeywordIsStillIdentifier() {
        // "setter" starts with "set" but is NOT the keyword SET
        List<Token> tokens = lex("setter");
        assertEquals(1, tokens.size());
        assertToken(tokens.get(0), TokenType.IDENTIFIER, "setter");
    }

    @Test
    void allFourKeywordsOnOneLine() {
        List<Token> tokens = lex("set show when loop");
        assertEquals(4, tokens.size());
        assertEquals(TokenType.SET, tokens.get(0).getType());
        assertEquals(TokenType.SHOW, tokens.get(1).getType());
        assertEquals(TokenType.WHEN, tokens.get(2).getType());
        assertEquals(TokenType.LOOP, tokens.get(3).getType());
    }

    // ── Identifiers ───────────────────────────────────────────────────────────

    @Test
    void simpleIdentifier() {
        List<Token> tokens = lex("x");
        assertToken(tokens.get(0), TokenType.IDENTIFIER, "x");
    }

    @Test
    void multiCharIdentifier() {
        List<Token> tokens = lex("result");
        assertToken(tokens.get(0), TokenType.IDENTIFIER, "result");
    }

    @Test
    void identifierWithUnderscore() {
        List<Token> tokens = lex("my_var");
        assertToken(tokens.get(0), TokenType.IDENTIFIER, "my_var");
    }

    @Test
    void identifierWithDigits() {
        List<Token> tokens = lex("x1");
        assertToken(tokens.get(0), TokenType.IDENTIFIER, "x1");
    }

    // ── Numbers ───────────────────────────────────────────────────────────────

    @Test
    void integerNumber() {
        List<Token> tokens = lex("10");
        assertToken(tokens.get(0), TokenType.NUMBER, "10");
    }

    @Test
    void singleDigitNumber() {
        List<Token> tokens = lex("3");
        assertToken(tokens.get(0), TokenType.NUMBER, "3");
    }

    @Test
    void largeNumber() {
        List<Token> tokens = lex("85");
        assertToken(tokens.get(0), TokenType.NUMBER, "85");
    }

    @Test
    void decimalNumber() {
        List<Token> tokens = lex("3.14");
        assertToken(tokens.get(0), TokenType.NUMBER, "3.14");
    }

    // ── String literals ───────────────────────────────────────────────────────

    @Test
    void simpleString() {
        List<Token> tokens = lex("\"Sitare\"");
        assertToken(tokens.get(0), TokenType.STRING, "Sitare");
    }

    @Test
    void stringWithSpaces() {
        List<Token> tokens = lex("\"Hello from ZARA\"");
        assertToken(tokens.get(0), TokenType.STRING, "Hello from ZARA");
    }

    @Test
    void stringValueHasNoQuotes() {
        List<Token> tokens = lex("\"Pass\"");
        String value = tokens.get(0).getValue();
        assertFalse(value.startsWith("\""), "String value must NOT start with a quote");
        assertFalse(value.endsWith("\""), "String value must NOT end with a quote");
        assertEquals("Pass", value);
    }

    // ── Operators ─────────────────────────────────────────────────────────────

    @Test
    void plusOperator() {
        assertToken(lex("+").get(0), TokenType.PLUS, "+");
    }

    @Test
    void minusOperator() {
        assertToken(lex("-").get(0), TokenType.MINUS, "-");
    }

    @Test
    void starOperator() {
        assertToken(lex("*").get(0), TokenType.STAR, "*");
    }

    @Test
    void slashOperator() {
        assertToken(lex("/").get(0), TokenType.SLASH, "/");
    }

    @Test
    void greaterOperator() {
        assertToken(lex(">").get(0), TokenType.GREATER, ">");
    }

    @Test
    void lessOperator() {
        assertToken(lex("<").get(0), TokenType.LESS, "<");
    }

    @Test
    void colonOperator() {
        assertToken(lex(":").get(0), TokenType.COLON, ":");
    }

    @Test
    void singleEqualsIsAssign() {
        assertToken(lex("=").get(0), TokenType.ASSIGN, "=");
    }

    @Test
    void doubleEqualsIsEquals() {
        assertToken(lex("==").get(0), TokenType.EQUALS, "==");
    }

    @Test
    void assignAndEqualsAreDistinct() {
        // "= ==" must produce ASSIGN then EQUALS, not two ASSIGNs
        List<Token> tokens = lex("= ==");
        assertEquals(2, tokens.size());
        assertEquals(TokenType.ASSIGN, tokens.get(0).getType());
        assertEquals(TokenType.EQUALS, tokens.get(1).getType());
    }

    // ── Whitespace handling ───────────────────────────────────────────────────

    @Test
    void spacesAroundTokensAreIgnored() {
        List<Token> tokens = lex("  x  +  y  ");
        assertEquals(3, tokens.size());
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals(TokenType.PLUS, tokens.get(1).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(2).getType());
    }

    @Test
    void tabsAroundTokensAreIgnored() {
        List<Token> tokens = lex("\tx\t+\ty\t");
        assertEquals(3, tokens.size());
    }

    @Test
    void newlineIsEmittedAsToken() {
        List<Token> tokens = lex("x\ny");
        assertEquals(3, tokens.size());
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals(TokenType.NEWLINE, tokens.get(1).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(2).getType());
    }

    @Test
    void indentedLineStillTokenisesCorrectly() {
        // after a block header, lines are indented with spaces
        // the Tokenizer must still produce the correct tokens
        List<Token> tokens = lex("    show \"Pass\"");
        assertEquals(2, tokens.size());
        assertEquals(TokenType.SHOW, tokens.get(0).getType());
        assertEquals(TokenType.STRING, tokens.get(1).getType());
        assertEquals("Pass", tokens.get(1).getValue());
    }

    // ── Line numbers ──────────────────────────────────────────────────────────

    @Test
    void tokenOnFirstLineHasLineOne() {
        List<Token> tokens = lex("set");
        assertEquals(1, tokens.get(0).getLine());
    }

    @Test
    void tokenOnSecondLineHasLineTwo() {
        List<Token> tokens = lex("set x = 10\nset y = 3");
        // find the second "set" token — it must be on line 2
        Token secondSet = tokens.stream()
                .filter(t -> t.getType() == TokenType.SET)
                .skip(1)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected two SET tokens"));
        assertEquals(2, secondSet.getLine());
    }

    @Test
    void newlineTokenIsOnCorrectLine() {
        List<Token> tokens = lex("x\ny");
        Token newline = tokens.get(1);
        assertEquals(TokenType.NEWLINE, newline.getType());
        assertEquals(1, newline.getLine()); // the \n is on line 1
    }

    // ── Full program tokenization (sample programs) ───────────────────────────

    @Test
    void program1ArithmeticAndVariables() {
        String source = "set x = 10\nset y = 3\nset result = x + y * 2\nshow result\n";
        List<Token> tokens = new Tokenizer(source).tokenize();

        // should not throw; spot-check a few key tokens
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == TokenType.SET));
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == TokenType.SHOW));
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == TokenType.PLUS));
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == TokenType.STAR));
        assertTrue(tokens.stream().anyMatch(t -> t.getValue().equals("result")));
        assertEquals(TokenType.EOF, tokens.get(tokens.size() - 1).getType());
    }

    @Test
    void program2StringOutput() {
        String source = "set name = \"Sitare\"\nshow name\nshow \"Hello from ZARA\"\n";
        List<Token> tokens = new Tokenizer(source).tokenize();

        // string token values must be stripped of quotes
        assertTrue(tokens.stream()
                .filter(t -> t.getType() == TokenType.STRING)
                .anyMatch(t -> t.getValue().equals("Sitare")));
        assertTrue(tokens.stream()
                .filter(t -> t.getType() == TokenType.STRING)
                .anyMatch(t -> t.getValue().equals("Hello from ZARA")));
    }

    @Test
    void program3Conditional() {
        String source = "set score = 85\nwhen score > 50:\n    show \"Pass\"\n";
        List<Token> tokens = new Tokenizer(source).tokenize();

        assertTrue(tokens.stream().anyMatch(t -> t.getType() == TokenType.WHEN));
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == TokenType.GREATER));
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == TokenType.COLON));
        assertTrue(tokens.stream().anyMatch(t -> t.getValue().equals("Pass")));
    }

    @Test
    void program4Loop() {
        String source = "set i = 1\nloop 4:\n    show i\n    set i = i + 1\n";
        List<Token> tokens = new Tokenizer(source).tokenize();

        assertTrue(tokens.stream().anyMatch(t -> t.getType() == TokenType.LOOP));
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == TokenType.COLON));
        // "4" must appear as a NUMBER token
        assertTrue(tokens.stream()
                .anyMatch(t -> t.getType() == TokenType.NUMBER && t.getValue().equals("4")));
    }
}