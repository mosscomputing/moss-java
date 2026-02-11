package com.mosscomputing.moss;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Agent representation.
 */
public class Agent {
    
    private String id;
    
    @JsonProperty("agent_id")
    private String agentId;
    
    @JsonProperty("display_name")
    private String displayName;
    
    private String status;
    private List<String> tags;
    private Map<String, Object> metadata;
    
    @JsonProperty("policy_id")
    private String policyId;
    
    @JsonProperty("total_signatures")
    private long totalSignatures;
    
    @JsonProperty("active_key_id")
    private String activeKeyId;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("last_seen_at")
    private String lastSeenAt;
    
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
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public String getPolicyId() {
        return policyId;
    }
    
    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }
    
    public long getTotalSignatures() {
        return totalSignatures;
    }
    
    public void setTotalSignatures(long totalSignatures) {
        this.totalSignatures = totalSignatures;
    }
    
    public String getActiveKeyId() {
        return activeKeyId;
    }
    
    public void setActiveKeyId(String activeKeyId) {
        this.activeKeyId = activeKeyId;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getLastSeenAt() {
        return lastSeenAt;
    }
    
    public void setLastSeenAt(String lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}
