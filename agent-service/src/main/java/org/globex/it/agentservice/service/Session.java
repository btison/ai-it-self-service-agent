package org.globex.it.agentservice.service;

import java.util.Map;

public class Session {

    private String sessionName;

    private String agentName;

    private String threadId;

    private Map<String, Object> conversationContext;

    private ConversationSession conversationSession;

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public Map<String, Object> getConversationContext() {
        return conversationContext;
    }

    public void setConversationContext(Map<String, Object> conversationContext) {
        this.conversationContext = conversationContext;
    }

    public ConversationSession getConversationSession() {
        return conversationSession;
    }

    public void setConversationSession(ConversationSession conversationSession) {
        this.conversationSession = conversationSession;
    }
}
