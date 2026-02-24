package org.globex.it.agentservice.model;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record AgentResponse(

        String requestId,

        String sessionId,

        String userId,

        Optional<String> agentId,

        String content,

        String responseType,

        Map<String, Object> metadata,

        Optional<Integer> processingTime,

        boolean requiresFollowUp,

        List<String> followUpActions,

        Long createdAt

) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String requestId;
        private String sessionId;
        private String userId;
        private Optional<String> agentId = Optional.empty();
        private String content;
        private String responseType = "message";
        private Map<String, Object> metadata;
        private Optional<Integer> processingTime = Optional.empty();
        private boolean requiresFollowUp = false;
        private List<String> followUpActions;
        private Long createdAt;

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

        public Builder agentId(String agentId) {
            this.agentId = agentId == null ? Optional.empty() : Optional.of(agentId);
            return this;
        }

        public Builder agentId(Optional<String> agentId) {
            this.agentId = agentId == null ? Optional.empty() : agentId;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder responseType(String responseType) {
            this.responseType = responseType;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata == null ? null : Map.copyOf(metadata);
            return this;
        }

        public Builder processingTime(Integer processingTime) {
            this.processingTime = processingTime == null ? Optional.empty() : Optional.of(processingTime);
            return this;
        }

        public Builder requiresFollowUp(boolean requiresFollowUp) {
            this.requiresFollowUp = requiresFollowUp;
            return this;
        }

        public Builder followUpActions(List<String> followUpActions) {
            this.followUpActions = followUpActions;
            return this;
        }

        public Builder createdAt(Long createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AgentResponse build() {
            return new AgentResponse(
                    requestId,
                    sessionId,
                    userId,
                    agentId,
                    content,
                    responseType,
                    metadata,
                    processingTime,
                    requiresFollowUp,
                    followUpActions == null ? null : List.copyOf(followUpActions),
                    createdAt
            );
        }
    }

    public JsonObject toJson() {

        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("requestId", requestId)
                .add("sessionId", sessionId)
                .add("userId", userId);

        agentId.ifPresent(s -> builder.add("agentId", s));
        builder.add("content", content)
                .add("responseType", responseType);
        if (metadata != null) {
            builder.add("metadata", Json.createObjectBuilder(metadata).build());
        }
        processingTime.ifPresent(integer -> builder.add("processingTime", integer));
        builder.add("requiresFollowUp", requiresFollowUp);
        if (followUpActions != null) {
            builder.add("followUpActions", Json.createArrayBuilder(followUpActions).build());
        }
        builder.add("createdAt", createdAt);

        return builder.build();

    }
}
