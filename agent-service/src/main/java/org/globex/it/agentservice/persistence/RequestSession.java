package org.globex.it.agentservice.persistence;

import jakarta.persistence.*;
import org.globex.it.agentservice.model.IntegrationType;
import org.globex.it.agentservice.model.SessionStatus;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "request_sessions")
@SequenceGenerator(name="RequestSessionsSeq", sequenceName="request_sessions_seq", allocationSize = 1)
@NamedQueries({
        @NamedQuery(name = "RequestSession.findBySessionId", query = "from RequestSession where sessionId = :sessionId"),
        @NamedQuery(name = "RequestSession.findBySessionIdAndStatus", query = "from RequestSession where sessionId = :sessionId and status = :status")
})
public class RequestSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator="RequestSessionsSeq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true, length = 36)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "integration_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private IntegrationType integrationType;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private SessionStatus status;

    @Column(name = "channel_id")
    private String channelId;

    @Column(name = "thread_id")
    private String threadId;

    @Column(name = "integration_metadata", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> integrationMetadata;

    @Column(name = "total_requests", nullable = false)
    private Integer totalRequests;

    @Column(name = "last_request_at")
    private Instant lastRequestAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "external_session_id")
    private String externalSessionId;

    @Column(name = "current_agent_id")
    private String currentAgentId;

    @Column(name = "conversation_thread_id")
    private String conversationThreadId;

    @Column(name = "user_context", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> userContext;

    @Column(name = "conversation_context", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> conversationContext;

    @Column(name = "last_request_id", length = 36)
    private String lastRequestId;

    @Column(name = "total_input_tokens", nullable = false)
    private Integer totalInputTokens = 0;

    @Column(name = "total_output_tokens", nullable = false)
    private Integer totalOutputTokens = 0;

    @Column(name = "total_tokens", nullable = false)
    private Integer totalTokens = 0;

    @Column(name = "llm_call_count", nullable = false)
    private Integer llmCallCount = 0;

    @Column(name = "max_input_tokens_per_call", nullable = false)
    private Integer maxInputTokensPerCall = 0;

    @Column(name = "max_output_tokens_per_call", nullable = false)
    private Integer maxOutputTokensPerCall = 0;

    @Column(name = "max_total_tokens_per_call", nullable = false)
    private Integer maxTotalTokensPerCall = 0;

    @Version
    @Column(name = "version")
    private int version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public IntegrationType getIntegrationType() {
        return integrationType;
    }

    public void setIntegrationType(IntegrationType integrationType) {
        this.integrationType = integrationType;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public Map<String, Object> getIntegrationMetadata() {
        return integrationMetadata;
    }

    public void setIntegrationMetadata(Map<String, Object> integrationMetadata) {
        this.integrationMetadata = integrationMetadata;
    }

    public Integer getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(Integer totalRequests) {
        this.totalRequests = totalRequests;
    }

    public Instant getLastRequestAt() {
        return lastRequestAt;
    }

    public void setLastRequestAt(Instant lastRequestAt) {
        this.lastRequestAt = lastRequestAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getExternalSessionId() {
        return externalSessionId;
    }

    public void setExternalSessionId(String externalSessionId) {
        this.externalSessionId = externalSessionId;
    }

    public String getCurrentAgentId() {
        return currentAgentId;
    }

    public void setCurrentAgentId(String currentAgentId) {
        this.currentAgentId = currentAgentId;
    }

    public String getConversationThreadId() {
        return conversationThreadId;
    }

    public void setConversationThreadId(String conversationThreadId) {
        this.conversationThreadId = conversationThreadId;
    }

    public Map<String, Object> getUserContext() {
        return userContext;
    }

    public void setUserContext(Map<String, Object> userContext) {
        this.userContext = userContext;
    }

    public Map<String, Object> getConversationContext() {
        return conversationContext;
    }

    public void setConversationContext(Map<String, Object> conversationContext) {
        this.conversationContext = conversationContext;
    }

    public String getLastRequestId() {
        return lastRequestId;
    }

    public void setLastRequestId(String lastRequestId) {
        this.lastRequestId = lastRequestId;
    }

    public Integer getTotalInputTokens() {
        return totalInputTokens;
    }

    public void setTotalInputTokens(Integer totalInputTokens) {
        this.totalInputTokens = totalInputTokens;
    }

    public Integer getTotalOutputTokens() {
        return totalOutputTokens;
    }

    public void setTotalOutputTokens(Integer totalOutputTokens) {
        this.totalOutputTokens = totalOutputTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(Integer totalTokens) {
        this.totalTokens = totalTokens;
    }

    public Integer getLlmCallCount() {
        return llmCallCount;
    }

    public void setLlmCallCount(Integer llmCallCount) {
        this.llmCallCount = llmCallCount;
    }

    public Integer getMaxInputTokensPerCall() {
        return maxInputTokensPerCall;
    }

    public void setMaxInputTokensPerCall(Integer maxInputTokensPerCall) {
        this.maxInputTokensPerCall = maxInputTokensPerCall;
    }

    public Integer getMaxOutputTokensPerCall() {
        return maxOutputTokensPerCall;
    }

    public void setMaxOutputTokensPerCall(Integer maxOutputTokensPerCall) {
        this.maxOutputTokensPerCall = maxOutputTokensPerCall;
    }

    public Integer getMaxTotalTokensPerCall() {
        return maxTotalTokensPerCall;
    }

    public void setMaxTotalTokensPerCall(Integer maxTotalTokensPerCall) {
        this.maxTotalTokensPerCall = maxTotalTokensPerCall;
    }

    public int getVersion() {
        return version;
    }
}
