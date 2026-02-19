# MOSS Java SDK

**Unsigned agent output is broken output.**

MOSS (Message-Origin Signing System) provides cryptographic signing for AI agents. Every output is signed with ML-DSA-44 (post-quantum), creating non-repudiable execution records with audit-grade provenance.

## Install

### Maven
```xml
<dependency>
    <groupId>com.mosscomputing</groupId>
    <artifactId>moss-sdk</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Gradle
```groovy
implementation 'com.mosscomputing:moss-sdk:0.1.0'
```

## Quick Start

```java
import com.mosscomputing.moss.*;
import java.util.Map;

public class Example {
    public static void main(String[] args) throws MossException {
        // Create client (uses MOSS_API_KEY env var if set)
        MossClient client = MossClient.builder()
            .apiKey(System.getenv("MOSS_API_KEY"))
            .build();
        
        // Sign any agent output
        SignResult result = client.sign(SignRequest.builder()
            .payload(Map.of("action", "transfer", "amount", 500))
            .agentId("agent-finance-01")
            .build());
        
        System.out.println("Signed! Hash: " + result.getEnvelope().getPayloadHash());
        System.out.println("Decision: " + result.getDecision());
        
        // Verify offline
        VerifyResult verifyResult = client.verify(
            Map.of("action", "transfer", "amount", 500),
            result.getEnvelope()
        );
        
        if (verifyResult.isValid()) {
            System.out.println("Verified! Signed by: " + verifyResult.getSubject());
        }
    }
}
```

## Enterprise Features

With an API key, you get policy evaluation, approval workflows, and audit logging:

```java
MossClient client = MossClient.builder()
    .apiKey(System.getenv("MOSS_API_KEY"))
    .build();

SignResult result = client.sign(SignRequest.builder()
    .payload(Map.of(
        "action", "high_risk_transfer",
        "amount", 1000000,
        "recipient", "external-account"
    ))
    .agentId("finance-bot")
    .action("transfer")
    .context(Map.of("user_id", "u123", "department", "finance"))
    .build());

switch (result.getDecision()) {
    case "allow":
        System.out.println("Action allowed");
        break;
    case "block":
        System.out.println("Action blocked: " + result.getReason());
        break;
    case "hold":
        System.out.println("Action held for approval: " + result.getActionId());
        break;
}
```

## Agent Lifecycle Management

```java
// Register a new agent
RegisterAgentRequest req = new RegisterAgentRequest();
req.setAgentId("my-new-agent");
req.setDisplayName("My New Agent");
req.setTags(List.of("production", "finance"));

RegisterAgentResult agent = client.registerAgent(req);
System.out.println("Signing secret (save this!): " + agent.getSigningSecret());

// Get agent details
Agent existing = client.getAgent("my-new-agent");
System.out.println("Status: " + existing.getStatus());
System.out.println("Signatures: " + existing.getTotalSignatures());

// Rotate key (returns new signing secret)
RotateKeyResult rotateResult = client.rotateAgentKey("my-new-agent", "quarterly rotation");
System.out.println("New signing secret: " + rotateResult.getSigningSecret());

// Suspend agent (can be reactivated)
client.suspendAgent("my-new-agent", "suspicious activity");

// Reactivate agent
client.reactivateAgent("my-new-agent");

// Permanently revoke agent
client.revokeAgent("my-new-agent", "compromised credentials");
```

## Envelope Format

Every signed action produces a verifiable envelope:

```java
Envelope envelope = result.getEnvelope();
envelope.getSpec();        // "moss-0001"
envelope.getVersion();     // 1
envelope.getAlg();         // "ML-DSA-44"
envelope.getSubject();     // Agent ID
envelope.getKeyVersion();  // Key version for rotation
envelope.getSeq();         // Sequence number
envelope.getIssuedAt();    // Unix timestamp
envelope.getPayloadHash(); // SHA-256 of payload
envelope.getSignature();   // ML-DSA-44 signature
```

## Configuration

```java
MossClient client = MossClient.builder()
    // API key for enterprise features (optional)
    .apiKey(System.getenv("MOSS_API_KEY"))
    
    // Custom API URL (optional)
    .baseUrl("https://moss-api.example.com")
    
    // Custom HTTP client (optional)
    .httpClient(HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build())
    
    .build();
```

## Error Handling

```java
try {
    SignResult result = client.sign(req);
} catch (MossException e) {
    System.err.println("MOSS error: " + e.getMessage());
}
```

## Pricing Tiers

| Tier | Price | Agents | Signatures | Retention |
|------|-------|--------|------------|-----------|
| **Free** | $0 | 5 | 1,000/day | 7 days |
| **Pro** | $1,499/mo | Unlimited | Unlimited | 1 year |
| **Enterprise** | Custom | Unlimited | Unlimited | 7 years |

*Annual billing: $1,249/mo (save $3,000/year)*

All new signups get a **14-day free trial** of Pro.

## Requirements

- Java 17 or higher

## Links

- [mosscomputing.com](https://mosscomputing.com) — Project site
- [app.mosscomputing.com](https://app.mosscomputing.com) — Dashboard
- [Python SDK](https://github.com/mosscomputing/moss) — moss-sdk
- [Go SDK](https://github.com/mosscomputing/moss-go) — moss-go

## License

Proprietary - See LICENSE for terms.

Copyright (c) 2025-2026 IAMPASS Inc. All Rights Reserved.
