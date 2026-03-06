package org.globex.it.agentservice.graph;

import org.bsc.langgraph4j.action.Command;
import org.bsc.langgraph4j.action.CommandAction;
import org.globex.it.agentservice.model.AIMessage;
import org.globex.it.agentservice.model.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class LlmProcessorCommandAction {

    public static CommandAction<org.globex.it.agentservice.graph.State> get(String currentState, String defaultTransition, Function<String, String> function) {
        return get(currentState, defaultTransition, null, function);
    }

    public static CommandAction<org.globex.it.agentservice.graph.State> get(String currentState, String defaultTransition, List<TransitionCondition> conditions, Function<String, String> function) {
        return (state, config) -> {
            //get last user message from state
            Message humanMessage = state.lastHumanMessage();
            String content = humanMessage == null ? "" : humanMessage.content();
            String response = function.apply(content);
            return processResponse(state, currentState, defaultTransition, conditions, response);
        };
    }

    private static Command processResponse(State state, String currentState, String defaultTransition, List<TransitionCondition> conditions, String response) {
        Map<String, Object> updateState = new HashMap<>();
        updateState.put(State.CURRENT_STATE, currentState);
        updateState = state.addMessage(new AIMessage(response), updateState);
        if (conditions == null || conditions.isEmpty()) {
            return new Command(defaultTransition, updateState);
        }
        String nextState = null;
        for (TransitionCondition condition : conditions) {
            if (condition.triggerPhrases().stream().noneMatch(s -> response.toLowerCase().contains(s.toLowerCase()))) {
                continue;
            }
            if (condition.excludePhrases().stream().anyMatch(s -> response.toLowerCase().contains(s.toLowerCase()))) {
                continue;
            }
            for (BaseAction action : condition.actions()) {
                updateState = action.execute(state, updateState);
            }
            nextState = condition.transition();

        }
        return new Command(nextState, updateState);
    }


}
