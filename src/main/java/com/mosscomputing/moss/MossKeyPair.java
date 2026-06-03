package com.mosscomputing.moss;

/**
 * An ML-DSA-44 key pair for local signing and verification.
 *
 * <p>Key sizes per FIPS 204 ML-DSA-44 parameter set:
 * public key = 1312 bytes, secret key = 2560 bytes.</p>
 *
 * <p>Internally, the BouncyCastle provider requires a 32-byte seed alongside
 * the 2560-byte expanded private key for JCA key reconstruction. This seed
 * is stored here so that signing operations can reconstruct the private key,
 * but it is NOT part of the FIPS 204 private key and is not exposed to
 * callers who only need the raw 1312/2560-byte key material.</p>
 */
public class MossKeyPair {

    /** FIPS 204 ML-DSA-44 public key size in bytes. */
    public static final int PUBLIC_KEY_SIZE = 1312;

    /** FIPS 204 ML-DSA-44 secret key size in bytes. */
    public static final int SECRET_KEY_SIZE = 2560;

    /** FIPS 204 ML-DSA-44 signature size in bytes. */
    public static final int SIGNATURE_SIZE = 2420;

    private final byte[] publicKey;
    private final byte[] secretKey;
    private final byte[] seed;   // BouncyCastle-internal, 32 bytes

    /**
     * Creates a new MossKeyPair.
     *
     * @param publicKey raw ML-DSA-44 public key (exactly 1312 bytes)
     * @param secretKey raw ML-DSA-44 secret key (exactly 2560 bytes)
     * @param seed      BouncyCastle seed (exactly 32 bytes, may be null for
     *                  externally-imported keys that don't need Java-side signing)
     * @throws IllegalArgumentException if key sizes do not match FIPS 204 parameters
     */
    public MossKeyPair(byte[] publicKey, byte[] secretKey, byte[] seed) {
        if (publicKey == null || publicKey.length != PUBLIC_KEY_SIZE) {
            throw new IllegalArgumentException(
                "ML-DSA-44 public key must be exactly " + PUBLIC_KEY_SIZE + " bytes, got "
                + (publicKey == null ? "null" : publicKey.length));
        }
        if (secretKey == null || secretKey.length != SECRET_KEY_SIZE) {
            throw new IllegalArgumentException(
                "ML-DSA-44 secret key must be exactly " + SECRET_KEY_SIZE + " bytes, got "
                + (secretKey == null ? "null" : secretKey.length));
        }
        if (seed != null && seed.length != 32) {
            throw new IllegalArgumentException(
                "ML-DSA-44 seed must be exactly 32 bytes or null, got " + seed.length);
        }
        this.publicKey = publicKey.clone();
        this.secretKey = secretKey.clone();
        this.seed = seed != null ? seed.clone() : null;
    }

    /**
     * Returns a copy of the raw ML-DSA-44 public key (1312 bytes).
     */
    public byte[] getPublicKey() {
        return publicKey.clone();
    }

    /**
     * Returns a copy of the raw ML-DSA-44 secret key (2560 bytes).
     */
    public byte[] getSecretKey() {
        return secretKey.clone();
    }

    /**
     * Returns a copy of the BouncyCastle seed (32 bytes), or null
     * if this key pair was imported from an external source.
     */
    byte[] getSeed() {
        return seed != null ? seed.clone() : null;
    }

    /**
     * Returns true if this key pair has a BouncyCastle seed available,
     * meaning it can be used for signing via the Java SDK.
     */
    public boolean hasSeed() {
        return seed != null;
    }
}
