package org.globex.it.agentservice.graph;

import java.util.List;
import java.util.function.Function;

public record LlmProcessorCommandActionParams(
        String currentState,
        String defaultTransition,
        String dataStorageField,
        List<TransitionCondition> conditions,
        Function<String, String> function
) {
}
