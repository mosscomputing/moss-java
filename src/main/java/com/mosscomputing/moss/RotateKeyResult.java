package com.mosscomputing.moss;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of rotating an agent's key.
 */
public class RotateKeyResult {
    
    @JsonProperty("agent_id")
    private String agentId;
    
    @JsonProperty("key_id")
    private String keyId;
    
    @JsonProperty("signing_secret")
    private String signingSecret;
    
    @JsonProperty("rotated_at")
    private String rotatedAt;
    
    public String getAgentId() {
        return agentId;
    }
    
    public void setAgentId(String agentId) {
        this.agentId = agentId;
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
    
    public String getRotatedAt() {
        return rotatedAt;
    }
    
    public void setRotatedAt(String rotatedAt) {
        this.rotatedAt = rotatedAt;
    }
}
