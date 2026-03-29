package zara.runtime;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> variables = new HashMap<>();

    public void set(String name, Object value) {
        variables.put(name, value);
    }

    public Object get(String name) {
        return variables.get(name);
    }
}