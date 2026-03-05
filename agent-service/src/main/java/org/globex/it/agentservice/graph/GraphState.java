package org.globex.it.agentservice.graph;

public record GraphState(
        String name,
        String type,
        boolean initialState,
        boolean terminalState,
        ResetBehavior resetBehavior
) {

    public boolean isWaitingState() {
        return type.equalsIgnoreCase("waiting");
    }

}
