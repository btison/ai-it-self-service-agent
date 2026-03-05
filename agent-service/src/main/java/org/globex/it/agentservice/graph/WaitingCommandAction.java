package org.globex.it.agentservice.graph;

import org.bsc.langgraph4j.action.Command;
import org.bsc.langgraph4j.action.CommandAction;

import java.util.HashMap;
import java.util.Map;

public class WaitingCommandAction {

    public static CommandAction<State> get(String currentState, String transition) {
        return (state, config) -> {
            Map<String, Object> updateState = new HashMap<>();
            updateState.put(State.CURRENT_STATE, currentState);
            String nextNode = transition == null ? "end" : transition;
            int humanCount = state.humanMessages().size();
            int lastProcessedGlobal = (int) state.value("_last_processed_human_count").orElse(0);
            boolean consumedThisInvoke = (boolean) state.value("_consumed_this_invoke").orElse(false);
            if (humanCount > lastProcessedGlobal && !consumedThisInvoke) {
                updateState.put("_last_processed_human_count", humanCount);
                updateState.put("_consumed_this_invoke", true);
                updateState.put(State.LAST_WAITING_NODE, null);
                return new Command(nextNode, updateState);
            }
            updateState.put(State.LAST_WAITING_NODE, currentState);
            return new Command("wait", updateState);
        };
    }

}
