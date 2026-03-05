package org.globex.it.agentservice.graph;

import java.util.*;

public class StateMachine {

    Map<String, GraphState> graphStates = new HashMap<>();

    List<BusinessField> businessFields = new ArrayList<>();

    public StateMachine addGraphState(GraphState graphState) {
        graphStates.put(graphState.name(),  graphState);
        return this;
    }

    public StateMachine addBusinessField(BusinessField businessField) {
        businessFields.add(businessField);
        return this;
    }

    public List<String> businessFieldNames() {
        return businessFields.stream().map(BusinessField::name).toList();
    }

    public BusinessField businessField(String name) {
        return businessFields.stream().filter(e -> e.name().equals(name)).findFirst().orElse(null);
    }

    public GraphState graphState(String name) {
        return graphStates.get(name);
    }

    public String initialStateName() {
        return initialState() == null ? "" : initialState().name();
    }

    public GraphState initialState() {
       return graphStates.values().stream().filter(GraphState::initialState).findFirst().orElse(null);
    }

    public GraphState terminalState() {
        return graphStates.values().stream().filter(GraphState::terminalState).findFirst().orElse(null);
    }

    public boolean isTerminalState(String state) {
        return state.equals(terminalState() == null ? null : terminalState().name());
    }

}
