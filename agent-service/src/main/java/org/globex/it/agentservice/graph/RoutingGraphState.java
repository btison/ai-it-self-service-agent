package org.globex.it.agentservice.graph;

import org.bsc.langgraph4j.state.AgentState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoutingGraphState extends AgentState {

    public RoutingGraphState(Map<String, Object> initData) {
        super(initData);
    }

    public List<String> userMessages() {
        //noinspection unchecked
        return (List<String>) value("userMessages").orElse(new ArrayList<>());
    }

    public List<String> systemMessages() {
        //noinspection unchecked
        return (List<String>) value("systemMessages").orElse(new ArrayList<>());
    }

    public String lastUserMessage() {
        if (userMessages().isEmpty()) {
            return null;
        } else {
            return userMessages().getLast();
        }
    }

    public String lastSystemMessage() {
        if (systemMessages().isEmpty()) {
            return null;
        } else {
            return systemMessages().getLast();
        }
    }

    public String intent() {
        return (String) value("intent").orElse("OTHER");
    }
}
