package org.globex.it.agentservice.model;

/**
 * Maps to PostgreSQL enum deliverystatus.
 */
public enum DeliveryStatus {
    PENDING,
    DELIVERED,
    FAILED,
    RETRYING,
    EXPIRED
}
