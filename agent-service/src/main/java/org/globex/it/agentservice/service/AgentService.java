package org.globex.it.agentservice.service;

import io.cloudevents.CloudEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.globex.it.agentservice.model.AgentResponse;
import org.globex.it.agentservice.model.NormalizedRequest;
import org.globex.it.agentservice.utils.RequestUtils;

@ApplicationScoped
public class AgentService {

    @Inject
    SessionManager sessionManager;

    public AgentResponse handleRequestEvent(CloudEvent cloudEvent) {

        NormalizedRequest normalizedRequest = RequestUtils.createNormalizedRequestFromData(cloudEvent);

        String response = sessionManager.handleMessage(normalizedRequest.content(), normalizedRequest.sessionId(), normalizedRequest.userId());

        return AgentResponse.builder()
                .requestId(normalizedRequest.requestId())
                .sessionId(normalizedRequest.sessionId())
                .userId(normalizedRequest.userId())
                .content(response)
                .createdAt(System.currentTimeMillis())
                .build();

    }

}
