package org.globex.it.agentservice.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record NormalizedRequest (

    String requestId,

    String sessionId,

    String userId,

    IntegrationType integrationType,

    String requestType,

    String content,

    Map<String, Object> integrationContext,

    Map<String, Object> userContext,

    Optional<String> targetAgentId,

    boolean requiresRouting,

    long createdAt

) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String requestId;
        private String sessionId;
        private String userId;
        private IntegrationType integrationType;
        private String requestType;
        private String content;
        private Map<String, String> integrationContext;
        private Map<String, String> userContext;
        private Optional<String> targetAgentId = Optional.empty();
        private boolean requiresRouting = true;
        private long createdAt;

        private Builder() {}

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder integrationType(IntegrationType integrationType) {
            this.integrationType = integrationType;
            return this;
        }

        public Builder requestType(String requestType) {
            this.requestType = requestType;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder integrationContext(Map<String, String> integrationContext) {
            this.integrationContext = integrationContext == null ? null : new HashMap<>(integrationContext);
            return this;
        }

        public Builder userContext(Map<String, String> userContext) {
            this.userContext = userContext == null ? null : new HashMap<>(userContext);
            return this;
        }

        public Builder targetAgentId(String targetAgentId) {
            this.targetAgentId = targetAgentId == null ? Optional.empty() : Optional.of(targetAgentId);
            return this;
        }

        public Builder targetAgentId(Optional<String> targetAgentId) {
            this.targetAgentId = targetAgentId == null ? Optional.empty() : targetAgentId;
            return this;
        }

        public Builder requiresRouting(boolean requiresRouting) {
            this.requiresRouting = requiresRouting;
            return this;
        }

        public Builder createdAt(long createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public NormalizedRequest build() {
            return new NormalizedRequest(
                    requestId,
                    sessionId,
                    userId,
                    integrationType,
                    requestType,
                    content,
                    integrationContext == null ? Map.of() : Map.copyOf(integrationContext),
                    userContext == null ? Map.of() : Map.copyOf(userContext),
                    targetAgentId,
                    requiresRouting,
                    createdAt
            );
        }
    }
}
