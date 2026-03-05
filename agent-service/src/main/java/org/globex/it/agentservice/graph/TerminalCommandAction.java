package org.globex.it.agentservice.graph;

import org.bsc.langgraph4j.action.Command;
import org.bsc.langgraph4j.action.CommandAction;

import java.util.HashMap;
import java.util.Map;

public class TerminalCommandAction {

    public static CommandAction<State> get(String currentState) {
        return (state, config) -> {
            Map<String, Object> updateState = new HashMap<>();
            updateState.put(State.CURRENT_STATE, currentState);
            return new Command(null, updateState);
        };
    }

}
