package com.mosscomputing.moss;

import java.time.Instant;

/**
 * Result of a verify operation.
 */
public class VerifyResult {
    
    private boolean valid;
    private String subject;
    private Instant issuedAt;
    private long sequence;
    private String error;
    
    private VerifyResult(Builder builder) {
        this.valid = builder.valid;
        this.subject = builder.subject;
        this.issuedAt = builder.issuedAt;
        this.sequence = builder.sequence;
        this.error = builder.error;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public Instant getIssuedAt() {
        return issuedAt;
    }
    
    public long getSequence() {
        return sequence;
    }
    
    public String getError() {
        return error;
    }
    
    public static class Builder {
        private boolean valid;
        private String subject;
        private Instant issuedAt;
        private long sequence;
        private String error;
        
        public Builder valid(boolean valid) {
            this.valid = valid;
            return this;
        }
        
        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }
        
        public Builder issuedAt(Instant issuedAt) {
            this.issuedAt = issuedAt;
            return this;
        }
        
        public Builder sequence(long sequence) {
            this.sequence = sequence;
            return this;
        }
        
        public Builder error(String error) {
            this.error = error;
            return this;
        }
        
        public VerifyResult build() {
            return new VerifyResult(this);
        }
    }
}
