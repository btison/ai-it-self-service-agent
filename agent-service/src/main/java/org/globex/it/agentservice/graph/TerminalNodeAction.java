package org.globex.it.agentservice.graph;

import org.bsc.langgraph4j.action.NodeAction;

import java.util.HashMap;
import java.util.Map;

public class TerminalNodeAction {

    public static NodeAction<State> get(String currentState) {
        return state -> {
            Map<String, Object> updateState = new HashMap<>();
            updateState.put(State.CURRENT_STATE, currentState);
            return State.updateState(state, updateState, null);
        };
    }

}
