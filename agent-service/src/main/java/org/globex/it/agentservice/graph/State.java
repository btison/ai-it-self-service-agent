package org.globex.it.agentservice.graph;

import io.vertx.core.json.JsonObject;
import org.bsc.langgraph4j.serializer.StateSerializer;
import org.bsc.langgraph4j.state.AgentState;
import org.globex.it.agentservice.model.AIMessage;
import org.globex.it.agentservice.model.HumanMessage;
import org.globex.it.agentservice.model.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class State extends AgentState  {

    public static String CURRENT_STATE = "current_state";
    public static String LAST_WAITING_NODE = "_last_waiting_node";

    public State(Map<String, Object> initData) {
        super(initData);
    }

    public List<Message> messages() {
        //noinspection unchecked
        List<String> serializedMessages = (List<String>) value("messages").orElse(new ArrayList<>());
        return serializedMessages.stream().map(this::fromJson).toList();
    }

    public Map<String, Object> addMessage(Message message, Map<String, Object> newState) {
        //noinspection unchecked
        List<String> serializedMessages = (List<String>) value("messages").orElse(new ArrayList<>());
        JsonObject json = new JsonObject();
        json.put("content", message.content());
        if (message instanceof HumanMessage) {
            json.put("role", "user");
        }  else if (message instanceof AIMessage) {
            json.put("role", "ai");
        }
        serializedMessages.add(json.encode());
        return State.updateState(newState, Map.of("messages", serializedMessages), null);
    }

    public Map<String, Object> addMessage(Message message) {
        //noinspection unchecked
        List<String> serializedMessages = (List<String>) value("messages").orElse(new ArrayList<>());
        serializedMessages.add(toJson(message));
        return State.updateState(this, Map.of("messages", serializedMessages), null);
    }

    public List<Message> humanMessages() {
        return messages().stream().filter(message -> message instanceof HumanMessage).toList();
    }

    public List<Message> aiMessages() {
        return messages().stream().filter(message -> message instanceof AIMessage).toList();
    }

    public Message lastHumanMessage() {
        List<Message> humanMessages = humanMessages();
        if (humanMessages.isEmpty()) {
            return null;
        } else {
            return humanMessages.getLast();
        }
    }

    public Message lastAIMessage() {
        if (aiMessages().isEmpty()) {
            return null;
        } else {
            return aiMessages().getLast();
        }
    }

    public Map<String, Object> setCurrentState(String currentState) {
        return State.updateState(this, Map.of(CURRENT_STATE, currentState), null);
    }

    public Map<String, Object> setCurrentState(String currentState, Map<String, Object> newState) {
        return State.updateState(newState, Map.of(CURRENT_STATE, currentState), null);
    }

    public String currentState() {
        return (String) value("current_state").orElse("");
    }

    public static State copy(State state, StateSerializer<State> serializer) {
        try {
            return serializer.cloneObject(state.data());
        } catch (IOException | ClassNotFoundException e) {
            return new State(state.data());
        }
    }

    String toJson(Message message) {
        JsonObject json = new JsonObject();
        json.put("content", message.content());
        if (message instanceof HumanMessage) {
            json.put("role", "user");
        }  else if (message instanceof AIMessage) {
            json.put("role", "ai");
        }
        return json.encode();
    }

    Message fromJson(String serialized) {
        JsonObject json = new JsonObject(serialized);
        String role = json.getString("role");
        String content = json.getString("content");
        if ("user".equals(role)) {
            return new HumanMessage(content);
        } else {
            return new AIMessage(content);
        }
    }
}
