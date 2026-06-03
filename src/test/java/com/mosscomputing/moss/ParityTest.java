package com.mosscomputing.moss;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parity tests verifying real ML-DSA-44 (FIPS 204) behaviour.
 *
 * <p>These tests guard against the historical fake-signing regression
 * where SHA-256 hashes were padded to 2420 bytes and length-only
 * verification was accepted. Every assertion below MUST pass against
 * the real BouncyCastle ML-DSA-44 implementation.</p>
 */
class ParityTest {

    private static final byte[] CANONICAL_PAYLOAD =
        "{\"event\":\"agent.action\",\"agent_id\":\"a-1\",\"ts\":\"2026-06-03T00:00:00Z\",\"payload\":{\"k\":\"v\"}}"
            .getBytes(StandardCharsets.UTF_8);

    // ── VAL-SDK-009: Key and signature sizes ────────────────────────────────

    @Test
    @DisplayName("sign produces a 2420-byte ML-DSA-44 signature and 1312-byte public key")
    void testSignatureAndPublicKeySizes() {
        MossKeyPair kp = MossSigner.generateKeyPair();

        assertEquals(MossKeyPair.PUBLIC_KEY_SIZE, kp.getPublicKey().length,
            "ML-DSA-44 public key must be exactly 1312 bytes");
        assertEquals(MossKeyPair.SECRET_KEY_SIZE, kp.getSecretKey().length,
            "ML-DSA-44 secret key must be exactly 2560 bytes");

        byte[] sig = MossSigner.sign(CANONICAL_PAYLOAD, kp);
        assertEquals(MossKeyPair.SIGNATURE_SIZE, sig.length,
            "ML-DSA-44 signature must be exactly 2420 bytes");
    }

    // ── VAL-SDK-010: Verify accepts honest, rejects tampered/zero ──────────

    @Test
    @DisplayName("verify accepts honest signatures and rejects tampered/zero ones")
    void testVerifyAcceptsRejectsAndZero() {
        MossKeyPair kp = MossSigner.generateKeyPair();
        byte[] pubKey = kp.getPublicKey();
        byte[] sig = MossSigner.sign(CANONICAL_PAYLOAD, kp);

        // Honest signature → must verify
        assertTrue(MossSigner.verify(CANONICAL_PAYLOAD, pubKey, sig),
            "verify must accept an honest ML-DSA-44 signature");

        // Bit-flipped signature → must reject
        byte[] tamperedSig = sig.clone();
        tamperedSig[0] ^= 0x01;
        assertFalse(MossSigner.verify(CANONICAL_PAYLOAD, pubKey, tamperedSig),
            "verify must reject a bit-flipped signature");

        // All-zeros signature → must reject (anti-fake regression guard)
        byte[] zeroSig = new byte[MossKeyPair.SIGNATURE_SIZE];
        assertFalse(MossSigner.verify(CANONICAL_PAYLOAD, pubKey, zeroSig),
            "verify must reject an all-zeros 2420-byte signature");
    }

    // ── Additional anti-fake regression guards ─────────────────────────────

    @Test
    @DisplayName("verify rejects a signature produced for a different payload")
    void testVerifyRejectsWrongPayload() {
        MossKeyPair kp = MossSigner.generateKeyPair();
        byte[] sig = MossSigner.sign(CANONICAL_PAYLOAD, kp);

        byte[] differentPayload = "{\"event\":\"other\"}".getBytes(StandardCharsets.UTF_8);
        assertFalse(MossSigner.verify(differentPayload, kp.getPublicKey(), sig),
            "verify must reject a signature bound to a different payload");
    }

    @Test
    @DisplayName("verify rejects a signature verified against the wrong public key")
    void testVerifyRejectsWrongPublicKey() {
        MossKeyPair kp1 = MossSigner.generateKeyPair();
        MossKeyPair kp2 = MossSigner.generateKeyPair();
        byte[] sig = MossSigner.sign(CANONICAL_PAYLOAD, kp1);

        assertFalse(MossSigner.verify(CANONICAL_PAYLOAD, kp2.getPublicKey(), sig),
            "verify must reject a signature verified against a different public key");
    }

    @Test
    @DisplayName("multiple signatures on the same payload are all valid")
    void testMultipleSignaturesValid() {
        MossKeyPair kp = MossSigner.generateKeyPair();
        byte[] pub = kp.getPublicKey();

        for (int i = 0; i < 5; i++) {
            byte[] sig = MossSigner.sign(CANONICAL_PAYLOAD, kp);
            assertEquals(MossKeyPair.SIGNATURE_SIZE, sig.length);
            assertTrue(MossSigner.verify(CANONICAL_PAYLOAD, pub, sig),
                "signature #" + i + " must verify");
        }
    }

    @Test
    @DisplayName("verify returns false for null or short signature, never throws")
    void testVerifyGracefulOnBadInput() {
        MossKeyPair kp = MossSigner.generateKeyPair();
        byte[] pub = kp.getPublicKey();

        assertFalse(MossSigner.verify(CANONICAL_PAYLOAD, pub, null),
            "null signature → false");
        assertFalse(MossSigner.verify(CANONICAL_PAYLOAD, pub, new byte[0]),
            "empty signature → false");
        assertFalse(MossSigner.verify(CANONICAL_PAYLOAD, pub, new byte[100]),
            "short signature → false");
    }
}
