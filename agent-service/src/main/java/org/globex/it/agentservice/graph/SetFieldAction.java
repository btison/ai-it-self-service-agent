package org.globex.it.agentservice.graph;

import java.util.Map;

public record SetFieldAction (
        String field,
        String value
) implements BaseAction {

    @Override
    public Map<String, Object> execute(State state, Map<String, Object> updateState) {
        updateState.put(field, value);
        return updateState;
    }
}
