package org.globex.it.agentservice.service;

import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class AuthorativeUserIdHolder {

    private String userId;

    public AuthorativeUserIdHolder() {}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
