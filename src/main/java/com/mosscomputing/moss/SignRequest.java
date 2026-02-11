package com.mosscomputing.moss;

import java.util.Map;

/**
 * Request to sign a payload.
 */
public class SignRequest {
    
    private Object payload;
    private String agentId;
    private String action;
    private Map<String, Object> context;
    
    private SignRequest(Builder builder) {
        this.payload = builder.payload;
        this.agentId = builder.agentId;
        this.action = builder.action;
        this.context = builder.context;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public Object getPayload() {
        return payload;
    }
    
    public String getAgentId() {
        return agentId;
    }
    
    public String getAction() {
        return action;
    }
    
    public Map<String, Object> getContext() {
        return context;
    }
    
    public static class Builder {
        private Object payload;
        private String agentId;
        private String action;
        private Map<String, Object> context;
        
        public Builder payload(Object payload) {
            this.payload = payload;
            return this;
        }
        
        public Builder agentId(String agentId) {
            this.agentId = agentId;
            return this;
        }
        
        public Builder action(String action) {
            this.action = action;
            return this;
        }
        
        public Builder context(Map<String, Object> context) {
            this.context = context;
            return this;
        }
        
        public SignRequest build() {
            return new SignRequest(this);
        }
    }
}
