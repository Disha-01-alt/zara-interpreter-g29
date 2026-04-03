package zara.runtime;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores variables and their values during program execution.
 */
public class Environment {
    private final Map<String, Object> variables = new HashMap<>();

    public void set(String name, Object value) {
        variables.put(name, value);
    }

    public Object get(String name) {
        if (!variables.containsKey(name)) {
            throw new RuntimeException("Variable '" + name + "' is not defined in the current environment.");
        }
        return variables.get(name);
    }
}