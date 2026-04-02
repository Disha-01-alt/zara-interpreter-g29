package zara.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvironmentTest {

    @Test
    void shouldStoreAndRetrieveValue() {
        Environment env = new Environment();
        env.set("x", 10.0);

        assertEquals(10.0, env.get("x"));
    }

    @Test
    void shouldOverwriteExistingValue() {
        Environment env = new Environment();
        env.set("x", 10.0);
        env.set("x", 99.0);

        assertEquals(99.0, env.get("x"));
    }

    @Test
    void shouldStoreStringValue() {
        Environment env = new Environment();
        env.set("name", "Sitare");

        assertEquals("Sitare", env.get("name"));
    }

    @Test
    void shouldThrowExceptionForUndefinedVariable() {
        Environment env = new Environment();

        assertThrows(RuntimeException.class, () -> env.get("unknown"));
    }

    @Test
    void shouldStoreMultipleVariables() {
        Environment env = new Environment();
        env.set("a", 1.0);
        env.set("b", 2.0);
        env.set("c", 3.0);

        assertEquals(1.0, env.get("a"));
        assertEquals(2.0, env.get("b"));
        assertEquals(3.0, env.get("c"));
    }
}