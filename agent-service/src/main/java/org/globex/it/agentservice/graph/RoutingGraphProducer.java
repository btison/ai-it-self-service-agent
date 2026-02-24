package org.globex.it.agentservice.graph;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.utils.EdgeMappings;
import org.globex.it.agentservice.agent.RoutingAgent;
import org.globex.it.agentservice.agent.RoutingHandleOtherRequestsAgent;
import org.globex.it.agentservice.agent.RoutingIdentifyIntentAgent;

import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.GraphDefinition.END;
import static org.bsc.langgraph4j.GraphDefinition.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@ApplicationScoped
public class RoutingGraphProducer {

    @Inject
    RoutingAgent routingAgent;

    @Inject
    RoutingIdentifyIntentAgent identifyIntentAgent;

    @Inject
    RoutingHandleOtherRequestsAgent handleOtherRequestsAgent;

    @Produces
    public CompiledGraph<RoutingGraphState> buildGraph() throws Exception {
        return compileGraph();
    }

    AsyncNodeAction<RoutingGraphState> identifyNeed = node_async(
            state -> {
                String greeting = identifyIntentAgent.greetAndIdentifyNeed();
                List<String> systemMessages = state.systemMessages();
                systemMessages.add(greeting);
                return Map.of("systemMessages", systemMessages);
            });

    AsyncNodeAction<RoutingGraphState> classifyIntent = node_async(
            state -> {
                String intent = routingAgent.classifyUserIntent(state.lastUserMessage());
                if ("LAPTOP_REFRESH".equals(intent)) {
                    return Map.of("intent", intent, "routing_decision", "laptop-refresh");
                } else if ("EMAIL_CHANGE".equals(intent)) {
                    return Map.of("intent", intent, "routing_decision", "email-change");
                } else {
                    return Map.of("intent", intent);
                }
            });

    AsyncNodeAction<RoutingGraphState> handleOtherRequests = node_async(
            state -> {
                String systemMessage = handleOtherRequestsAgent.handleOtherRequest(state.lastUserMessage());
                List<String> systemMessages = state.systemMessages();
                systemMessages.add(systemMessage);
                return Map.of("systemMessages", systemMessages);
            });

    AsyncNodeAction<RoutingGraphState> waitForUserInput = node_async(
            state -> Map.of());

    AsyncEdgeAction<RoutingGraphState> handleIntent = edge_async(
            RoutingGraphState::intent);

    private CompiledGraph<RoutingGraphState> compileGraph() throws GraphStateException {
        var compileConfig = CompileConfig.builder()
                .checkpointSaver(new MemorySaver())
                .interruptAfter("wait_for_input")
                .releaseThread(true)
                .build();

        return new StateGraph<>(RoutingGraphState::new)
                .addNode("identify_need", identifyNeed)
                .addNode("wait_for_input", waitForUserInput)
                .addNode("classify_intent", classifyIntent)
                .addNode("handle_other_requests", handleOtherRequests)
                .addEdge(START, "identify_need")
                .addEdge("identify_need", "wait_for_input")
                .addEdge("wait_for_input", "classify_intent")
                .addEdge("handle_other_requests", "wait_for_input")
                .addConditionalEdges("classify_intent", handleIntent, EdgeMappings.builder()
                        .to(END,  "LAPTOP_REFRESH")
                        .to(END,  "EMAIL_CHANGE")
                        .to("handle_other_requests", "OTHER")
                        .build())
                .compile(compileConfig);

    }

}
