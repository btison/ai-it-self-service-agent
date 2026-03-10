package org.globex.it.agentservice.graph;

import io.vertx.core.json.JsonObject;
import org.bsc.langgraph4j.action.Command;
import org.bsc.langgraph4j.action.CommandAction;
import org.globex.it.agentservice.model.AIMessage;
import org.globex.it.agentservice.model.Message;

import java.util.HashMap;
import java.util.Map;

public class LlmProcessorCommandAction {

    public static CommandAction<org.globex.it.agentservice.graph.State> get(LlmProcessorCommandActionParams params) {
        return (state, config) -> {
            //get last user message from state
            Message humanMessage = state.lastHumanMessage();
            String content = humanMessage == null ? "" : humanMessage.content();
            String response = params.function().apply(content);
            return processResponse(state, params, response);
        };
    }

    private static Command processResponse(State state, LlmProcessorCommandActionParams params, String response) {
        Map<String, Object> updateState = new HashMap<>();
        updateState.put(State.CURRENT_STATE, params.currentState());
        updateState = state.addMessage(new AIMessage(response), updateState);
        if (params.dataStorageField() != null && !params.dataStorageField().isEmpty()) {
            updateState.put(params.dataStorageField(), new JsonObject().put("response", response).encode());
        }
        if (params.conditions() == null || params.conditions().isEmpty()) {
            return new Command(params.defaultTransition(), updateState);
        }
        String nextState = null;
        for (TransitionCondition condition : params.conditions()) {
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
