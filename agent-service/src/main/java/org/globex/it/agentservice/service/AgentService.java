package org.globex.it.agentservice.service;

import io.cloudevents.CloudEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.state.StateSnapshot;
import org.globex.it.agentservice.graph.RoutingGraphState;
import org.globex.it.agentservice.model.AgentResponse;
import org.globex.it.agentservice.model.NormalizedRequest;
import org.globex.it.agentservice.utils.RequestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AgentService {

    @Inject
    CompiledGraph<RoutingGraphState> graph;

    Map<String, RunnableConfig> configs = new HashMap<>();

    public AgentResponse handleRequestEvent(CloudEvent cloudEvent) {

        NormalizedRequest normalizedRequest = RequestUtils.createNormalizedRequestFromData(cloudEvent);

        NodeOutput<RoutingGraphState> output;

        // need better way to detect the start of a conversation. Is the absence of a RunnableConfig enough?
        if (!configs.containsKey(normalizedRequest.sessionId())) {

            RunnableConfig config = RunnableConfig.builder()
                    .threadId(normalizedRequest.sessionId())
                    .streamMode(CompiledGraph.StreamMode.SNAPSHOTS)
                    .build();
            configs.put(normalizedRequest.sessionId(), config);
            output = executeGraphUpdateConfigAndFetchLastState(config, Map.of());
        } else {
            RunnableConfig config = configs.get(normalizedRequest.sessionId());
            RoutingGraphState state = graph.getState(config).state();
            List<String> userMessages = state.userMessages();
            userMessages.add(normalizedRequest.content());

            RunnableConfig updatedConfig;
            try {
                updatedConfig = graph.updateState(config, Map.of("userMessages", userMessages));
                configs.put(normalizedRequest.sessionId(), updatedConfig);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            output = executeGraphUpdateConfigAndFetchLastState(updatedConfig, null);
        }

        String outputMessage = output.state().lastSystemMessage();

        return AgentResponse.builder()
                .requestId(normalizedRequest.requestId())
                .sessionId(normalizedRequest.sessionId())
                .userId(normalizedRequest.userId())
                .content(outputMessage)
                .createdAt(System.currentTimeMillis())
                .build();
    }

    private NodeOutput<RoutingGraphState> executeGraphUpdateConfigAndFetchLastState(RunnableConfig config, Map<String, Object> input ) {

        // Get last state
        var LastNodeOutput = graph.stream(input, config)
                .stream()
                .peek( o -> System.out.println("data: " + o.state().data()) )
                .reduce((a, b) -> b)
                .orElseThrow();

        // Get last snapshot
        RunnableConfig lastConfig = graph.lastStateOf(config)
                .map(StateSnapshot::config)
                .orElse( null) ;

        if (lastConfig == null) {
            configs.remove(config.threadId().orElseThrow());
        } else {
            configs.put(config.threadId().orElseThrow(), lastConfig);
        }

        return LastNodeOutput;
    }

}
