package com.mosscomputing.moss;

/**
 * Result of a sign operation.
 */
public class SignResult {
    
    private Envelope envelope;
    private boolean allowed;
    private boolean blocked;
    private boolean held;
    private String decision;
    private String reason;
    private String actionId;
    private String evidenceId;
    private boolean signatureValid;
    
    private SignResult(Builder builder) {
        this.envelope = builder.envelope;
        this.allowed = builder.allowed;
        this.blocked = builder.blocked;
        this.held = builder.held;
        this.decision = builder.decision;
        this.reason = builder.reason;
        this.actionId = builder.actionId;
        this.evidenceId = builder.evidenceId;
        this.signatureValid = builder.signatureValid;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public Envelope getEnvelope() {
        return envelope;
    }
    
    public boolean isAllowed() {
        return allowed;
    }
    
    public boolean isBlocked() {
        return blocked;
    }
    
    public boolean isHeld() {
        return held;
    }
    
    public String getDecision() {
        return decision;
    }
    
    public String getReason() {
        return reason;
    }
    
    public String getActionId() {
        return actionId;
    }
    
    public String getEvidenceId() {
        return evidenceId;
    }
    
    public boolean isSignatureValid() {
        return signatureValid;
    }
    
    public static class Builder {
        private Envelope envelope;
        private boolean allowed;
        private boolean blocked;
        private boolean held;
        private String decision;
        private String reason;
        private String actionId;
        private String evidenceId;
        private boolean signatureValid;
        
        public Builder envelope(Envelope envelope) {
            this.envelope = envelope;
            return this;
        }
        
        public Builder allowed(boolean allowed) {
            this.allowed = allowed;
            return this;
        }
        
        public Builder blocked(boolean blocked) {
            this.blocked = blocked;
            return this;
        }
        
        public Builder held(boolean held) {
            this.held = held;
            return this;
        }
        
        public Builder decision(String decision) {
            this.decision = decision;
            return this;
        }
        
        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }
        
        public Builder actionId(String actionId) {
            this.actionId = actionId;
            return this;
        }
        
        public Builder evidenceId(String evidenceId) {
            this.evidenceId = evidenceId;
            return this;
        }
        
        public Builder signatureValid(boolean signatureValid) {
            this.signatureValid = signatureValid;
            return this;
        }
        
        public SignResult build() {
            return new SignResult(this);
        }
    }
}
