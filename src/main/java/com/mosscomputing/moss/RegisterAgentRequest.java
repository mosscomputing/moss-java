package com.mosscomputing.moss;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Request to register a new agent.
 */
public class RegisterAgentRequest {
    
    @JsonProperty("agent_id")
    private String agentId;
    
    @JsonProperty("display_name")
    private String displayName;
    
    private List<String> tags;
    private Map<String, Object> metadata;
    
    @JsonProperty("policy_id")
    private String policyId;
    
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
}
