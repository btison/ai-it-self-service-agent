package org.globex.it.agentservice.graph;

import java.util.Map;

public interface BaseAction {

    Map<String, Object> execute(State state, Map<String, Object> updateState);

}
