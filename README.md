# moss-java

MOSS SDK for Java — cryptographic signing for AI agent actions using ML-DSA-44 (FIPS 204).

## Overview

MOSS provides cryptographic signing for AI agent outputs using **ML-DSA-44**, a post-quantum digital signature algorithm standardized in **NIST FIPS 204**. Every agent action is signed to create non-repudiable execution records with audit-grade provenance. Unsigned agent output is broken output.

### Cryptographic Parameters (ML-DSA-44 / FIPS 204)

| Parameter | Size |
|-----------|------|
| Public key | **1312 bytes** |
| Secret key | **2560 bytes** |
| Signature | **2420 bytes** |

These are the FIPS 204 canonical parameter sizes for the ML-DSA-44 parameter set. All signing and verification operations produce or consume values of exactly these sizes.

## Installation

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

### Local signing (no API key required)

```java
import com.mosscomputing.moss.*;

public class Example {
    public static void main(String[] args) throws MossException {
        // Generate an ML-DSA-44 key pair (pub=1312 bytes, sec=2560 bytes)
        MossKeyPair keyPair = MossSigner.generateKeyPair();
        
        byte[] message = "hello world".getBytes();
        
        // Sign — produces a 2420-byte ML-DSA-44 signature
        byte[] signature = MossSigner.sign(message, keyPair);
        System.out.println("Signature length: " + signature.length); // 2420
        
        // Verify — real cryptographic verification (not a length check)
        boolean valid = MossSigner.verify(message, keyPair.getPublicKey(), signature);
        System.out.println("Valid: " + valid); // true
        
        // Tampered message — verification fails
        byte[] tampered = "hello world!".getBytes();
        boolean tamperedValid = MossSigner.verify(tampered, keyPair.getPublicKey(), signature);
        System.out.println("Tampered valid: " + tamperedValid); // false
    }
}
```

### Enterprise mode (with API key)

```java
import com.mosscomputing.moss.*;
import java.util.Map;

public class EnterpriseExample {
    public static void main(String[] args) throws MossException {
        MossClient client = MossClient.builder()
            .apiKey(System.getenv("MOSS_API_KEY"))
            .build();
        
        // Sign any agent output
        SignResult result = client.sign(SignRequest.builder()
            .payload(Map.of("action", "transfer", "amount", 500))
            .agentId("agent-finance-01")
            .build());
        
        System.out.println("Signed! Hash: " + result.getEnvelope().getPayloadHash());

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

## Features

- **Cryptographic signing (ML-DSA-44 / FIPS 204)** — Post-quantum secure signatures with verified parameter sizes (public key 1312, secret key 2560, signature 2420 bytes)
- **Local key generation** — `MossSigner.generateKeyPair()` creates ML-DSA-44 key pairs for offline signing
- **Real verification** — `MossSigner.verify()` performs full ML-DSA-44 signature verification; rejects bit-flipped and all-zeros signatures
- **Policy evaluation** — Server-side policy checks with allow/block/hold decisions
- **Evidence chain linking** — Sequential signatures with payload hashes for audit trails
- **Offline verification** — Verify signatures locally without network calls

## API Reference

### MossSigner (ML-DSA-44 primitives)

```java
// Generate key pair
MossKeyPair keyPair = MossSigner.generateKeyPair();
// keyPair.getPublicKey().length == 1312
// keyPair.getSecretKey().length == 2560

// Sign
byte[] signature = MossSigner.sign(message, keyPair);
// signature.length == 2420

// Verify
boolean valid = MossSigner.verify(message, keyPair.getPublicKey(), signature);
// true for honest signatures, false for tampered/zero signatures
```

### MossClient

```java
// Create client
MossClient client = MossClient.builder()
    .apiKey(System.getenv("MOSS_API_KEY"))
    .baseUrl("https://api.mosscomputing.com") // optional
    .build();
```

### Sign

```java
SignResult result = client.sign(SignRequest.builder()
    .payload(payload)           // Any serializable object
    .agentId("agent-id")       // Agent identifier
    .action("action-name")     // Optional action type
    .context(Map.of(...))      // Optional metadata
    .build());
```

### Verify

```java
VerifyResult verifyResult = client.verify(payload, envelope);
// verifyResult.isValid(): true if signature valid
// verifyResult.getSubject(): signing agent ID
```

### Envelope

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
envelope.getSignature();   // ML-DSA-44 signature (base64url, 2420 bytes raw)
```

## Configuration

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `MOSS_API_KEY` | API key for enterprise features | None |
| `MOSS_API_URL` | Custom API endpoint | `https://api.mosscomputing.com` |

**Requirements:** Java 17 or higher

**Dependencies:** `bcprov-jdk18on:1.84` (BouncyCastle — ML-DSA-44 / FIPS 204 implementation)

## Links

- Documentation: [docs.mosscomputing.com/sdks/java](https://docs.mosscomputing.com/sdks/java)
- Dashboard: [app.mosscomputing.com](https://app.mosscomputing.com)
- Python SDK: [pypi.org/project/moss-sdk](https://pypi.org/project/moss-sdk/)

## License

Business Source License 1.1 - See LICENSE file.

Copyright (c) 2025-2026 IAMPASS Inc.
