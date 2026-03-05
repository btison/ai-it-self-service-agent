package org.globex.it.agentservice.model;

import java.io.Serializable;

public record HumanMessage(
        String content
) implements Message {
}
