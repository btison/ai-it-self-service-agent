package org.globex.it.agentservice.service;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.RunnableConfig;
import org.globex.it.agentservice.agent.Agent;
import org.globex.it.agentservice.graph.GraphState;
import org.globex.it.agentservice.graph.ResetBehavior;
import org.globex.it.agentservice.graph.State;
import org.globex.it.agentservice.model.HumanMessage;

import java.util.*;

public class ConversationSession {

    private String threadId;

    private String authorativeUserId;

    private Agent agent;

    public String getThreadId() {
        return threadId;
    }

    public String getAuthorativeUserId() {
        return authorativeUserId;
    }

    public Agent getAgent() {
        return agent;
    }

    public String sendMessage(String message) {
        // get state from graph for current threadId
        // if state is empty/null, create initial state
        State result = null;
        State state = getState();
        if (state == null) {
            state = createInitialState();
            if (state.messages().isEmpty()) {
                Map<String, Object> updatedState = state.addMessage(new HumanMessage(message));
                GraphState initialState = agent.stateMachine().initialState();
                if (!initialState.isWaitingState()) {
                    updatedState.put("_last_processed_human_count", 1);
                    updatedState.put("_consumed_this_invoke", false);
                }
                RunnableConfig config = RunnableConfig.builder()
                        .threadId(threadId)
                        .streamMode(CompiledGraph.StreamMode.SNAPSHOTS)
                        .build();
                result = agent.graph().invoke(State.updateState(state, updatedState, null), config).orElse(null);
            }
        } else {
            State stateCopy = State.copy(state, agent.graph().stateGraph.getStateSerializer());

            Map<String, Object> updatedState = stateCopy.addMessage(new HumanMessage(message));
            updatedState.put("_consumed_this_invoke", false);
            RunnableConfig config = RunnableConfig.builder()
                    .threadId(threadId)
                    .streamMode(CompiledGraph.StreamMode.SNAPSHOTS)
                    .build();
            result = agent.graph().invoke(State.updateState(stateCopy, updatedState, null), config).orElse(null);
        }
        String lastAIMessage = null;
        if (result != null) {
            lastAIMessage = result.lastAIMessage().content();
            // check if conversation ended and needs reset
            if (agent.stateMachine().isTerminalState(state.currentState())) {
                GraphState terminalState = agent.stateMachine().terminalState();
                ResetBehavior resetBehavior = terminalState.resetBehavior();
                if (resetBehavior != null)  {
                    Map<String, Object> stateValues = resetStateForNewConversation(resetBehavior, agent);
//                    Map<String, Object> stateValuesWithoutCurrentState = stateValues.entrySet().stream()
//                            .filter(entry -> !entry.getKey().equals("current_state")).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    RunnableConfig config = RunnableConfig.builder()
                            .threadId(threadId)
                            .streamMode(CompiledGraph.StreamMode.SNAPSHOTS)
                            .build();
                    try {
                        agent.graph().updateState(config, stateValues);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                if (lastAIMessage != null) {
                    lastAIMessage = lastAIMessage + "\n\n[Conversation completed! Starting new conversation...]";
                } else {
                    lastAIMessage = "Conversation completed! Starting new conversation...";
                }
            }

        }
        return Objects.requireNonNullElse(lastAIMessage, "No response received from agent");
    }

    State getState() {
        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .streamMode(CompiledGraph.StreamMode.SNAPSHOTS)
                .build();
        return agent.graph().stateOf(config).map(NodeOutput::state).orElse(null);
    }

    private State createInitialState() {
        Map<String, Object> values = Map.of("current_state", agent.stateMachine().initialStateName());
        return new State(values);
    }

    private Map<String, Object> resetStateForNewConversation(ResetBehavior resetBehavior, Agent agent) {
        Map<String, Object> values = new HashMap<>();
        if (resetBehavior.clearData().contains("messages")) {
            values.put("aiMessages", new ArrayList<>());
            values.put("humanMessages", new ArrayList<>());
        }
        if (resetBehavior.clearData().contains("current_state")) {
            values.put("current_state", resetBehavior.resetState());
        }
        List<String> businessFieldNames = agent.stateMachine().businessFieldNames();
        for (String field : resetBehavior.clearData()) {
            if (businessFieldNames.contains(field)) {
                values.put(field, agent.stateMachine().businessField(field).defaultValue());
            }
        }
        return values;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final ConversationSession session = new ConversationSession();

        public Builder threadId(String threadId) {
            session.threadId = threadId;
            return this;
        }

        public Builder authorativeUserId(String authorativeUserId) {
            session.authorativeUserId = authorativeUserId;
            return this;
        }

        public Builder agent(Agent agent) {
            session.agent = agent;
            return this;
        }

        public ConversationSession build() {
            return session;
        }

    }

}
