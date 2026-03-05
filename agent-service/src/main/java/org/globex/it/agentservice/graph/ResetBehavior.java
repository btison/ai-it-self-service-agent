package org.globex.it.agentservice.graph;

import java.util.List;

public record ResetBehavior(
        String resetState,
        List<String> clearData
) {
}
