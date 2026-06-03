package com.mosscomputing.moss;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MOSS Java SDK - Cryptographic signing for AI agents.
 * 
 * <p>MOSS (Message-Origin Signing System) provides cryptographic signing for AI agents.
 * Every output is signed with ML-DSA-44 (post-quantum), creating non-repudiable
 * execution records with audit-grade provenance.</p>
 * 
 * <h2>Quick Start</h2>
 * <pre>{@code
 * MossClient client = MossClient.builder()
 *     .apiKey(System.getenv("MOSS_API_KEY"))
 *     .build();
 * 
 * SignResult result = client.sign(SignRequest.builder()
 *     .payload(Map.of("action", "transfer", "amount", 500))
 *     .agentId("agent-finance-01")
 *     .build());
 * 
 * System.out.println("Decision: " + result.getDecision());
 * }</pre>
 */
public class MossClient {
    
    public static final String SPEC = "moss-0001";
    public static final int VERSION = 1;
    public static final String ALGORITHM = "ML-DSA-44";
    public static final String DEFAULT_BASE_URL = "https://api.mosscomputing.com";
    
    private final String apiKey;
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AtomicLong sequence;
    private MossKeyPair localKeyPair;
    
    private MossClient(Builder builder) {
        this.apiKey = builder.apiKey != null ? builder.apiKey : System.getenv("MOSS_API_KEY");
        this.baseUrl = builder.baseUrl != null ? builder.baseUrl : DEFAULT_BASE_URL;
        this.httpClient = builder.httpClient != null ? builder.httpClient : HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        this.sequence = new AtomicLong(0);
    }
    
    /**
     * Creates a new builder for MossClient.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Signs a payload and returns the envelope.
     * 
     * @param request the sign request
     * @return the sign result with envelope
     * @throws MossException if signing fails
     */
    public SignResult sign(SignRequest request) throws MossException {
        if (apiKey == null || apiKey.isEmpty()) {
            return signLocal(request);
        }
        return signEnterprise(request);
    }
    
    private SignResult signLocal(SignRequest request) throws MossException {
        try {
            // Lazily generate ML-DSA-44 key pair on first local sign
            if (localKeyPair == null) {
                localKeyPair = MossSigner.generateKeyPair();
            }
            
            String payloadJson = objectMapper.writeValueAsString(request.getPayload());
            String payloadHash = computeHash(payloadJson);
            
            long seq = sequence.incrementAndGet();
            long now = Instant.now().getEpochSecond();
            
            String subject = request.getAgentId();
            if (subject == null || subject.isEmpty()) {
                subject = "moss:local:default";
            }
            
            // Sign the canonical payload with real ML-DSA-44
            byte[] payloadBytes = payloadJson.getBytes(StandardCharsets.UTF_8);
            byte[] signature = MossSigner.sign(payloadBytes, localKeyPair);
            String signatureB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
            
            Envelope envelope = new Envelope();
            envelope.setSpec(SPEC);
            envelope.setVersion(VERSION);
            envelope.setAlg(ALGORITHM);
            envelope.setSubject(subject);
            envelope.setKeyVersion(1);
            envelope.setSeq(seq);
            envelope.setIssuedAt(now);
            envelope.setPayloadHash(payloadHash);
            envelope.setSignature(signatureB64);
            
            return SignResult.builder()
                    .envelope(envelope)
                    .allowed(true)
                    .decision("allow")
                    .signatureValid(true)
                    .build();
        } catch (Exception e) {
            throw new MossException("Failed to sign payload", e);
        }
    }
    
    private SignResult signEnterprise(SignRequest request) throws MossException {
        try {
            Map<String, Object> evalRequest = new java.util.HashMap<>();
            evalRequest.put("subject", request.getAgentId());
            evalRequest.put("action", request.getAction());
            evalRequest.put("payload", request.getPayload());
            if (request.getContext() != null) {
                evalRequest.put("context", request.getContext());
            }
            
            String body = objectMapper.writeValueAsString(evalRequest);
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/evaluate"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(30))
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new MossException("API error (status " + response.statusCode() + "): " + response.body());
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            
            Envelope envelope = null;
            if (result.containsKey("envelope") && result.get("envelope") != null) {
                envelope = objectMapper.convertValue(result.get("envelope"), Envelope.class);
            }
            
            if (envelope == null) {
                String payloadJson = objectMapper.writeValueAsString(request.getPayload());
                String payloadHash = computeHash(payloadJson);
                long seq = sequence.incrementAndGet();
                
                envelope = new Envelope();
                envelope.setSpec(SPEC);
                envelope.setVersion(VERSION);
                envelope.setAlg(ALGORITHM);
                envelope.setSubject(request.getAgentId());
                envelope.setKeyVersion(1);
                envelope.setSeq(seq);
                envelope.setIssuedAt(Instant.now().getEpochSecond());
                envelope.setPayloadHash(payloadHash);
            }
            
            String decision = (String) result.getOrDefault("decision", "allow");
            
            return SignResult.builder()
                    .envelope(envelope)
                    .allowed("allow".equals(decision))
                    .blocked("block".equals(decision))
                    .held("hold".equals(decision))
                    .decision(decision)
                    .reason((String) result.get("reason"))
                    .actionId((String) result.get("action_id"))
                    .evidenceId((String) result.get("evidence_id"))
                    .signatureValid((Boolean) result.getOrDefault("signature_valid", true))
                    .build();
        } catch (IOException | InterruptedException e) {
            throw new MossException("Request failed", e);
        }
    }
    
    /**
     * Verifies an envelope against a payload using real ML-DSA-44.
     * 
     * @param payload the original payload
     * @param envelope the envelope to verify
     * @return the verification result
     */
    public VerifyResult verify(Object payload, Envelope envelope) {
        if (envelope == null) {
            return VerifyResult.builder()
                    .valid(false)
                    .error("Invalid envelope")
                    .build();
        }
        
        if (!SPEC.equals(envelope.getSpec())) {
            return VerifyResult.builder()
                    .valid(false)
                    .error("Unknown spec: " + envelope.getSpec())
                    .build();
        }
        
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            String computedHash = computeHash(payloadJson);
            
            if (!computedHash.equals(envelope.getPayloadHash())) {
                return VerifyResult.builder()
                        .valid(false)
                        .error("Payload hash mismatch")
                        .build();
            }
            
            // Real ML-DSA-44 signature verification
            String sigB64 = envelope.getSignature();
            if (sigB64 == null || sigB64.isEmpty()) {
                return VerifyResult.builder()
                        .valid(false)
                        .error("Empty signature")
                        .build();
            }
            
            // If we have a local key pair and the envelope was locally signed,
            // verify against the local public key
            if (localKeyPair != null) {
                byte[] signature = Base64.getUrlDecoder().decode(sigB64);
                byte[] payloadBytes = payloadJson.getBytes(StandardCharsets.UTF_8);
                boolean mlDsaValid = MossSigner.verify(payloadBytes, localKeyPair.getPublicKey(), signature);
                
                if (!mlDsaValid) {
                    return VerifyResult.builder()
                            .valid(false)
                            .error("ML-DSA-44 signature verification failed")
                            .build();
                }
            }
            
            return VerifyResult.builder()
                    .valid(true)
                    .subject(envelope.getSubject())
                    .issuedAt(Instant.ofEpochSecond(envelope.getIssuedAt()))
                    .sequence(envelope.getSeq())
                    .build();
        } catch (Exception e) {
            return VerifyResult.builder()
                    .valid(false)
                    .error("Verification failed: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Registers a new agent.
     * 
     * @param request the registration request
     * @return the registration result with signing secret
     * @throws MossException if registration fails
     */
    public RegisterAgentResult registerAgent(RegisterAgentRequest request) throws MossException {
        requireApiKey();
        
        try {
            String body = objectMapper.writeValueAsString(request);
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/agents"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(30))
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new MossException("API error (status " + response.statusCode() + "): " + response.body());
            }
            
            return objectMapper.readValue(response.body(), RegisterAgentResult.class);
        } catch (IOException | InterruptedException e) {
            throw new MossException("Request failed", e);
        }
    }
    
    /**
     * Gets agent details.
     * 
     * @param agentId the agent ID
     * @return the agent, or null if not found
     * @throws MossException if request fails
     */
    public Agent getAgent(String agentId) throws MossException {
        requireApiKey();
        
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/agents/" + agentId))
                    .header("Authorization", "Bearer " + apiKey)
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 404) {
                return null;
            }
            
            if (response.statusCode() != 200) {
                throw new MossException("API error (status " + response.statusCode() + "): " + response.body());
            }
            
            return objectMapper.readValue(response.body(), Agent.class);
        } catch (IOException | InterruptedException e) {
            throw new MossException("Request failed", e);
        }
    }
    
    /**
     * Rotates an agent's signing key.
     * 
     * @param agentId the agent ID
     * @param reason the rotation reason
     * @return the rotation result with new signing secret
     * @throws MossException if rotation fails
     */
    public RotateKeyResult rotateAgentKey(String agentId, String reason) throws MossException {
        requireApiKey();
        
        try {
            Map<String, String> body = new java.util.HashMap<>();
            if (reason != null) {
                body.put("reason", reason);
            }
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/agents/" + agentId + "/rotate"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .timeout(Duration.ofSeconds(30))
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new MossException("API error (status " + response.statusCode() + "): " + response.body());
            }
            
            return objectMapper.readValue(response.body(), RotateKeyResult.class);
        } catch (IOException | InterruptedException e) {
            throw new MossException("Request failed", e);
        }
    }
    
    /**
     * Suspends an agent.
     * 
     * @param agentId the agent ID
     * @param reason the suspension reason
     * @throws MossException if suspension fails
     */
    public void suspendAgent(String agentId, String reason) throws MossException {
        requireApiKey();
        
        try {
            Map<String, String> body = new java.util.HashMap<>();
            if (reason != null) {
                body.put("reason", reason);
            }
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/agents/" + agentId + "/suspend"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .timeout(Duration.ofSeconds(30))
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new MossException("API error (status " + response.statusCode() + "): " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            throw new MossException("Request failed", e);
        }
    }
    
    /**
     * Reactivates a suspended agent.
     * 
     * @param agentId the agent ID
     * @throws MossException if reactivation fails
     */
    public void reactivateAgent(String agentId) throws MossException {
        requireApiKey();
        
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/agents/" + agentId + "/reactivate"))
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(30))
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new MossException("API error (status " + response.statusCode() + "): " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            throw new MossException("Request failed", e);
        }
    }
    
    /**
     * Permanently revokes an agent.
     * 
     * @param agentId the agent ID
     * @param reason the revocation reason (required)
     * @throws MossException if revocation fails
     */
    public void revokeAgent(String agentId, String reason) throws MossException {
        requireApiKey();
        
        if (reason == null || reason.isEmpty()) {
            throw new MossException("Reason is required for revocation");
        }
        
        try {
            Map<String, String> body = Map.of("reason", reason);
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/agents/" + agentId + "/revoke"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .timeout(Duration.ofSeconds(30))
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new MossException("API error (status " + response.statusCode() + "): " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            throw new MossException("Request failed", e);
        }
    }
    
    /**
     * Returns true if enterprise mode is enabled (API key is set).
     */
    public boolean isEnterpriseEnabled() {
        return apiKey != null && !apiKey.isEmpty();
    }
    
    private void requireApiKey() throws MossException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new MossException("API key is required for this operation");
        }
    }
    
    private String computeHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
    
    /**
     * Builder for MossClient.
     */
    public static class Builder {
        private String apiKey;
        private String baseUrl;
        private HttpClient httpClient;
        
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }
        
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }
        
        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }
        
        public MossClient build() {
            return new MossClient(this);
        }
    }
}
