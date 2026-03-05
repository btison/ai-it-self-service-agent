package org.globex.it.agentservice.graph;

public record BusinessField(
        String name,
        Class type,
        Object defaultValue
) {
}
