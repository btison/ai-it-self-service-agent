package org.globex.it.agentservice.agent;

import org.bsc.langgraph4j.CompiledGraph;
import org.globex.it.agentservice.graph.StateMachine;
import org.globex.it.agentservice.graph.State;

public interface Agent {

    CompiledGraph<State> graph();

    StateMachine stateMachine();

}
