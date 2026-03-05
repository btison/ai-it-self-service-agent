package org.globex.it.agentservice.graph;

import org.bsc.langgraph4j.action.Command;
import org.bsc.langgraph4j.action.CommandAction;

import java.util.Optional;

public class ResumeDispatcherCommandAction {

    public static CommandAction<State> get(String initialState) {
        return (state, config) -> {
            Optional<String> lastWaitingNode = state.value(State.LAST_WAITING_NODE);
            return lastWaitingNode.map(s -> new Command(s, state.data())).orElseGet(() -> new Command(initialState, state.data()));
        };

    }

}
