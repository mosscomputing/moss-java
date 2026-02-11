package com.mosscomputing.moss;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * MOSS signature envelope.
 */
public class Envelope {
    
    private String spec;
    private int version;
    private String alg;
    private String subject;
    
    @JsonProperty("key_version")
    private int keyVersion;
    
    private long seq;
    
    @JsonProperty("issued_at")
    private long issuedAt;
    
    @JsonProperty("payload_hash")
    private String payloadHash;
    
    private String signature;
    
    public String getSpec() {
        return spec;
    }
    
    public void setSpec(String spec) {
        this.spec = spec;
    }
    
    public int getVersion() {
        return version;
    }
    
    public void setVersion(int version) {
        this.version = version;
    }
    
    public String getAlg() {
        return alg;
    }
    
    public void setAlg(String alg) {
        this.alg = alg;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public int getKeyVersion() {
        return keyVersion;
    }
    
    public void setKeyVersion(int keyVersion) {
        this.keyVersion = keyVersion;
    }
    
    public long getSeq() {
        return seq;
    }
    
    public void setSeq(long seq) {
        this.seq = seq;
    }
    
    public long getIssuedAt() {
        return issuedAt;
    }
    
    public void setIssuedAt(long issuedAt) {
        this.issuedAt = issuedAt;
    }
    
    public String getPayloadHash() {
        return payloadHash;
    }
    
    public void setPayloadHash(String payloadHash) {
        this.payloadHash = payloadHash;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }
}
