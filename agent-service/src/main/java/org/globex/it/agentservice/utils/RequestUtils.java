package org.globex.it.agentservice.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.core.data.BytesCloudEventData;
import io.cloudevents.jackson.JsonCloudEventData;
import org.globex.it.agentservice.model.IntegrationType;
import org.globex.it.agentservice.model.NormalizedRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class RequestUtils {

    public static NormalizedRequest createNormalizedRequestFromData(CloudEvent cloudEvent) {

        //TODO Validation
        ObjectMapper mapper = new ObjectMapper();

        CloudEventData cloudEventData = cloudEvent.getData();
        JsonNode node;
        if (cloudEventData instanceof JsonCloudEventData) {
            node = ((JsonCloudEventData) cloudEvent.getData()).getNode();
        } else if (cloudEventData instanceof BytesCloudEventData) {
            try {
                node = mapper.readValue(cloudEvent.getData().toBytes(), JsonNode.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {
            throw new RuntimeException("Cloud event data type not supported: " + cloudEventData.getClass());
        }

        return NormalizedRequest.builder()
                .requestId(node.get("requestId").asText())
                .sessionId(node.get("sessionId").asText())
                .userId(node.get("userId").asText())
                .integrationType(IntegrationType.valueOf(node.get("integrationType").asText()))
                .requestType(node.get("requestType").asText())
                .content(node.get("content").asText())
                .integrationContext(node.get("integrationContext") == null ? Collections.emptyMap() : mapper.convertValue(node.get("integrationContext"), Map.class))
                .userContext(node.get("userContext") == null ? Collections.emptyMap() : mapper.convertValue(node.get("userContext"), Map.class))
                .targetAgentId(node.get("targetAgentId") == null ? Optional.empty() : Optional.of(node.get("targetAgentId").asText()))
                .createdAt(node.get("createdAt").asLong())
                .build();
    }

}
