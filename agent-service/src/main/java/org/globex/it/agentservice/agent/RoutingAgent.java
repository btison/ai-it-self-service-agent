package org.globex.it.agentservice.agent;

import io.quarkus.arc.Unremovable;
import io.smallrye.common.annotation.Identifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bsc.langgraph4j.CompiledGraph;
import org.globex.it.agentservice.graph.StateMachine;
import org.globex.it.agentservice.graph.State;

@ApplicationScoped
@Identifier("routing-agent")
@Unremovable
public class RoutingAgent implements Agent {

    @Inject @Identifier("routing-agent")
    CompiledGraph<State> graph;

    @Inject @Identifier("routing-agent")
    StateMachine stateMachine;

    @Override
    public CompiledGraph<State> graph() {
        return graph;
    }

    @Override
    public StateMachine stateMachine() {
        return stateMachine;
    }
}
