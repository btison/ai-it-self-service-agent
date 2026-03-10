package org.globex.it.agentservice.service;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.globex.it.agentservice.agent.Agent;
import org.globex.it.agentservice.agent.AgentManager;
import org.globex.it.agentservice.graph.State;
import org.globex.it.agentservice.model.SessionStatus;
import org.globex.it.agentservice.persistence.RequestSession;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class SessionManager {

    final static String ROUTING_AGENT = "routing-agent";

    @Inject
    EntityManager em;

    @Inject
    AgentManager agentManager;

    @Inject
    AuthorativeUserIdHolder authorativeUserIdHolder;

    public String handleMessage(String message, String sessionId, String userId) {

        String messagePreview = message.length() < 100 ? message : message.substring(0, 100);
        Log.infof("Handling response message; user %s; message: %s; session: %s", userId, messagePreview, sessionId);

        Session session = null;

        // try to resume an existing session from the database
        if (sessionId != null) {
            session = resumeSessionFromDatabase(sessionId, userId);
            if (session != null) {
                Log.infof("Resumed existing session; session: %s; threadId: %s; agent: %s", sessionId, session.getThreadId(), session.getAgentName());
            }
        }
        if (session == null) {
            Log.infof("Creating initial session for responses mode; session: %s", userId, sessionId);
            session = createInitialSession(userId);
            updateSessionInDatabase(sessionId, session);
        }
        boolean shouldReset = false;
        State state = session.getConversationSession().getState();
        if (state != null) {
            shouldReset = (boolean) state.value("_should_return_to_routing").orElse(false);
        }
        if (shouldReset) {
            Log.infof("Specialist task complete, returning to routing agent; user: %s; agent: %s", userId, session.getAgentName());
            resetConversationState(sessionId);
            return handleMessage(message, sessionId, userId);
        }
        // todo: token context

        String response = session.getConversationSession().sendMessage(message);

        response = handleRouting(response, session, message, sessionId, userId);

        Log.infof("Response message processed successfully; userId: %s; agent: %s; response length: %s", userId, session.getAgentName(), response.length());
        return response;
    }

    private Session createInitialSession(String userId) {
        String sessionName = generateSessionName(userId, null);
        Agent routingAgent = agentManager.getAgent(ROUTING_AGENT);
        ConversationSession conversationSession = createSessionForAgent(routingAgent, sessionName, userId, null);
        Session session = new Session();
        session.setSessionName(sessionName);
        session.setConversationSession(conversationSession);
        session.setAgentName(ROUTING_AGENT);
        session.setThreadId(conversationSession.getThreadId());
        return session;
    }

    @Transactional
    void updateSessionInDatabase(String sessionId, Session session) {
        Query findRequestSessionBySessionId = em.createNamedQuery("RequestSession.findBySessionId");
        findRequestSessionBySessionId.setParameter("sessionId", sessionId);
        List requestSessions = findRequestSessionBySessionId.getResultList();
        if (requestSessions.isEmpty()) {
            Log.warnf("No response session found for sessionId: %s", sessionId);
            return;
        }
        RequestSession requestSession = (RequestSession)requestSessions.getFirst();
        requestSession.setConversationThreadId(session.getConversationSession().getThreadId());
        requestSession.setCurrentAgentId(session.getAgentName());
        requestSession.setUpdatedAt(Instant.now());
        requestSession.setConversationContext(Map.of("agent_name",session.getAgentName(),
                "session_type","responses_api","last_updated",
                OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString()));
        authorativeUserIdHolder.setUserId(requestSession.getUser().getPrimaryEmail());
    }

    private ConversationSession createSessionForAgent(Agent agent, String sessionName, String userId, String threadId) {
        String sessionThreadId = threadId == null ? UUID.randomUUID().toString() : threadId;
        return ConversationSession.builder()
                .threadId(sessionThreadId)
                .agent(agent)
                .build();
    }

    private String generateSessionName(String userId, String agentName) {
        UUID uuid = UUID.randomUUID();
        if (agentName != null && !agentName.isEmpty()) {
            return "session-" + userId + "-" + agentName + "-" + uuid;
        } else  {
            return "session-" + userId + "-" + uuid;
        }
    }

    Session resumeSessionFromDatabase(String sessionId, String userId) {
        RequestSession requestSession = loadSessionFromDatabase(sessionId);
        if (requestSession == null) {
            return null;
        }
        if (requestSession.getCurrentAgentId() == null || requestSession.getCurrentAgentId().isEmpty()
                || requestSession.getConversationThreadId() == null || requestSession.getConversationThreadId().isEmpty()) {
            Log.warnf("Session with id %s found but missing required fields", sessionId);
            return null;
        }
        Session session = new Session();
        session.setAgentName(requestSession.getCurrentAgentId());
        session.setThreadId(requestSession.getConversationThreadId());
        session.setConversationContext(requestSession.getConversationContext());
        Agent agent = agentManager.getAgent(requestSession.getCurrentAgentId());
        if (agent == null) {
            Log.warnf("Agent with id %s not found. Cannot resume session", requestSession.getCurrentAgentId());
            return null;
        }
        String sessionName = generateSessionName(userId, requestSession.getCurrentAgentId());
        ConversationSession conversationSession = createSessionForAgent(agent, sessionName, userId, requestSession.getConversationThreadId());
        session.setConversationSession(conversationSession);
        // check if the conversation is in a terminal state
        State state = conversationSession.getState();
        if (state != null) {
            String currentState = (String) state.value("current_state").orElse(null);
            if (currentState != null && !currentState.isEmpty()) {
                if (!session.getAgentName().equals(ROUTING_AGENT)
                        && session.getConversationSession().getAgent().stateMachine().isTerminalState(currentState)) {
                    Log.infof("Resumed specialist session is in terminal state - resetting to routing agent; sessionId: %s; agent: %s; current state: %",
                            sessionId, session.getAgentName(), currentState);
                    resetConversationState(sessionId);
                    return null;
                }
            }
        }
        return session;
    }

    @Transactional
    RequestSession loadSessionFromDatabase(String sessionId) {
        Query findRequestSessionBySessionId = em.createNamedQuery("RequestSession.findBySessionId");
        findRequestSessionBySessionId.setParameter("sessionId", sessionId);
        List requestSessions = findRequestSessionBySessionId.getResultList();
        if (requestSessions.isEmpty()) {
            Log.warnf("No response session found for sessionId: %s", sessionId);
            return null;
        }
        RequestSession requestSession = (RequestSession)requestSessions.getFirst();
        authorativeUserIdHolder.setUserId(requestSession.getUser().getPrimaryEmail());
        return requestSession;
    }

    private boolean isSpecialistSession(Session session) {
        return !ROUTING_AGENT.equals(session.getAgentName());
    }

    private boolean isRoutingSession(Session session) {
        return ROUTING_AGENT.equals(session.getAgentName());
    }

    @Transactional
    void resetConversationState(String sessionId) {
        Query findRequestSessionBySessionIdAndStatus = em.createNamedQuery("RequestSession.findBySessionIdAndStatus");
        findRequestSessionBySessionIdAndStatus.setParameter("sessionId", sessionId).setParameter("status", SessionStatus.ACTIVE);
        List requestSessions = findRequestSessionBySessionIdAndStatus.getResultList();
        if (requestSessions.isEmpty()) {
            Log.warnf("No response session found for sessionId: %s", sessionId);
            return;
        }
        RequestSession requestSession = (RequestSession)requestSessions.getFirst();
        requestSession.setCurrentAgentId(null);
        requestSession.setConversationThreadId(null);
        requestSession.setUpdatedAt(Instant.now());
    }

    String handleRouting(String response, Session session, String message, String sessionId, String userId) {
        String routedAgent = null;
        // check state
        State state = session.getConversationSession().getState();
        if (state != null) {
            String routingDecision = state.value("routing_decision").orElse("").toString();
            if (!routingDecision.isEmpty() && agentManager.getAgent(routingDecision) != null
            ) {
                routedAgent = routingDecision;
                Log.infof("Found routing decision for agent: %s", routedAgent);
            }
        }
        // fallback: check response
        if (routedAgent == null) {
            String signal = response.strip().toLowerCase();
            if (agentManager.getAgent(signal) != null) {
                routedAgent = signal;
                Log.infof("Found routing decision in response (fallback); agent: %s", routedAgent);
            }
        }
        if (routedAgent != null) {
            if (routedAgent.equals(ROUTING_AGENT) && isSpecialistSession(session)) {
                resetConversationState(sessionId);
                return handleMessage("Hi", sessionId, userId);
            }
            if (!routedAgent.equals(ROUTING_AGENT) && isRoutingSession(session)) {
                return routeToSpecialist(routedAgent, message, sessionId, userId);
            }
        }
        if (isSpecialistSession(session)) {
            String responseLower = response.toLowerCase();
            List<String> terminationMarkers = List.of("conversation completed", "starting new conversation", "task_complete_return_to_router");
            if (terminationMarkers.stream().anyMatch(responseLower::contains)) {
                String cleanedResponse = "";
                if (response.contains("\n")) {
                    String[] lines = response.split("\n");
                    List<String> cleaned = Arrays.stream(lines).filter(s -> terminationMarkers.stream().noneMatch(s::contains)).toList();
                    StringBuilder stringBuilder = new StringBuilder();
                    cleaned.forEach(s -> stringBuilder.append(s).append("\n"));
                    cleanedResponse = stringBuilder.toString().strip();
                }
                if (!cleanedResponse.isEmpty()) {
                    return cleanedResponse;
                } else {
                    resetConversationState(sessionId);
                    return handleMessage("Hi", sessionId, userId);
                }
            }
        }
        return response;
    }

    private String routeToSpecialist(String agentName, String message, String sessionId, String userId) {
        Agent agent = agentManager.getAgent(agentName);
        String sessionName = generateSessionName(userId, agentName);
        ConversationSession conversationSession = createSessionForAgent(agent, sessionName, userId, null);
        Session session = new Session();
        session.setConversationSession(conversationSession);
        session.setThreadId(conversationSession.getThreadId());
        session.setAgentName(agentName);
        session.setSessionName(sessionName);
        updateSessionInDatabase(sessionId, session);
        return session.getConversationSession().sendMessage(message);
    }
}
