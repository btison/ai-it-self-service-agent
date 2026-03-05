package org.globex.it.agentservice.graph;

import org.globex.it.agentservice.model.AIMessage;

import java.util.Map;

public record AddMessageAction (
        String message
) implements BaseAction {

    @Override
    public Map<String, Object> execute(State state, Map<String, Object> updateState) {
        return state.addMessage(new AIMessage(message), updateState);
    }
}
