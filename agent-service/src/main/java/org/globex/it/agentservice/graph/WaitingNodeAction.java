package org.globex.it.agentservice.graph;

import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;

public class WaitingNodeAction {

    public static NodeAction<State> get() {
        return AgentState::data;
    }

}
