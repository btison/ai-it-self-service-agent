package org.globex.it.agentservice.model;

import java.io.Serializable;

public record AIMessage(
        String content
) implements Message {
}
