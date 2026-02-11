package com.mosscomputing.moss;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of registering an agent.
 */
public class RegisterAgentResult {
    
    private String id;
    
    @JsonProperty("agent_id")
    private String agentId;
    
    @JsonProperty("display_name")
    private String displayName;
    
    private String status;
    
    @JsonProperty("key_id")
    private String keyId;
    
    @JsonProperty("signing_secret")
    private String signingSecret;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getAgentId() {
        return agentId;
    }
    
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getKeyId() {
        return keyId;
    }
    
    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }
    
    public String getSigningSecret() {
        return signingSecret;
    }
    
    public void setSigningSecret(String signingSecret) {
        this.signingSecret = signingSecret;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
