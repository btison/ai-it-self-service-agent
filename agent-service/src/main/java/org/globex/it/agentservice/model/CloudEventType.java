package org.globex.it.agentservice.model;

/**
 * Enumeration of CloudEvent type values used by the self-service agent.
 */
public enum CloudEventType {

    REQUEST_CREATED("com.self-service-agent.request.created"),
    REQUEST_PROCESSING("com.self-service-agent.request.processing");

    private final String type;

    CloudEventType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }

    /**
     * Resolves an event type string to the corresponding enum constant, or null if no match.
     */
    public static CloudEventType fromType(String type) {
        if (type == null) {
            return null;
        }
        for (CloudEventType value : values()) {
            if (value.type.equals(type)) {
                return value;
            }
        }
        return null;
    }
}
