package com.mosscomputing.moss;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jcajce.provider.asymmetric.mldsa.BCMLDSAPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.mldsa.BCMLDSAPublicKey;
import org.bouncycastle.jcajce.spec.MLDSAParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * ML-DSA-44 (FIPS 204) cryptographic operations for MOSS.
 *
 * <p>Provides key generation, signing, and verification using the
 * BouncyCastle {@code bcprov-jdk18on:1.84} provider.</p>
 *
 * <h2>Parameter sizes (ML-DSA-44)</h2>
 * <ul>
 *   <li>Public key: 1312 bytes</li>
 *   <li>Secret key: 2560 bytes</li>
 *   <li>Signature: 2420 bytes</li>
 * </ul>
 */
public final class MossSigner {

    /** JCA algorithm name for ML-DSA. */
    private static final String ALGORITHM = "ML-DSA";

    /** BouncyCastle provider name. */
    private static final String PROVIDER = "BC";

    /** OID for ML-DSA-44 in the NIST PQC arc. */
    private static final ASN1ObjectIdentifier ML_DSA_44_OID =
        new ASN1ObjectIdentifier("2.16.840.1.101.3.4.3.17");

    static {
        if (Security.getProvider(PROVIDER) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private MossSigner() {
        // Utility class — no instances
    }

    /**
     * Generates a new ML-DSA-44 key pair.
     *
     * @return a {@link MossKeyPair} with a 1312-byte public key and 2560-byte secret key
     */
    public static MossKeyPair generateKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALGORITHM, PROVIDER);
            kpg.initialize(MLDSAParameterSpec.ml_dsa_44, new SecureRandom());
            KeyPair kp = kpg.generateKeyPair();

            BCMLDSAPublicKey bcPub = (BCMLDSAPublicKey) kp.getPublic();
            BCMLDSAPrivateKey bcPriv = (BCMLDSAPrivateKey) kp.getPrivate();

            byte[] rawPub = bcPub.getPublicData();     // 1312 bytes
            byte[] rawPriv = bcPriv.getPrivateData();  // 2560 bytes
            byte[] seed = bcPriv.getSeed();             // 32 bytes

            return new MossKeyPair(rawPub, rawPriv, seed);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate ML-DSA-44 key pair", e);
        }
    }

    /**
     * Signs a message using ML-DSA-44.
     *
     * @param message   the message bytes to sign
     * @param keyPair   the key pair to sign with (must have a seed)
     * @return the 2420-byte ML-DSA-44 signature
     */
    public static byte[] sign(byte[] message, MossKeyPair keyPair) {
        if (keyPair == null) {
            throw new IllegalArgumentException("keyPair must not be null");
        }
        try {
            PrivateKey privKey = reconstructPrivateKey(
                keyPair.getSecretKey(), keyPair.getSeed());
            Signature signer = Signature.getInstance(ALGORITHM, PROVIDER);
            signer.initSign(privKey);
            signer.update(message);
            return signer.sign();
        } catch (Exception e) {
            throw new RuntimeException("ML-DSA-44 signing failed", e);
        }
    }

    /**
     * Signs a message using ML-DSA-44 with a raw secret key and seed.
     *
     * @param message   the message bytes to sign
     * @param secretKey the raw 2560-byte ML-DSA-44 secret key
     * @param seed      the 32-byte BouncyCastle seed (required for signing)
     * @return the 2420-byte ML-DSA-44 signature
     */
    public static byte[] sign(byte[] message, byte[] secretKey, byte[] seed) {
        if (secretKey == null || secretKey.length != MossKeyPair.SECRET_KEY_SIZE) {
            throw new IllegalArgumentException(
                "ML-DSA-44 secret key must be exactly " + MossKeyPair.SECRET_KEY_SIZE + " bytes");
        }
        if (seed == null || seed.length != 32) {
            throw new IllegalArgumentException(
                "ML-DSA-44 seed must be exactly 32 bytes for signing");
        }
        try {
            PrivateKey privKey = reconstructPrivateKey(secretKey, seed);
            Signature signer = Signature.getInstance(ALGORITHM, PROVIDER);
            signer.initSign(privKey);
            signer.update(message);
            return signer.sign();
        } catch (Exception e) {
            throw new RuntimeException("ML-DSA-44 signing failed", e);
        }
    }

    /**
     * Verifies an ML-DSA-44 signature.
     *
     * @param message   the original message bytes
     * @param publicKey the raw 1312-byte ML-DSA-44 public key
     * @param signature the ML-DSA-44 signature to verify
     * @return {@code true} if the signature is valid, {@code false} otherwise
     */
    public static boolean verify(byte[] message, byte[] publicKey, byte[] signature) {
        if (publicKey == null || publicKey.length != MossKeyPair.PUBLIC_KEY_SIZE) {
            throw new IllegalArgumentException(
                "ML-DSA-44 public key must be exactly " + MossKeyPair.PUBLIC_KEY_SIZE + " bytes");
        }
        if (signature == null) {
            return false;
        }
        // Early-reject obviously wrong-sized signatures (anti-fake guard)
        if (signature.length != MossKeyPair.SIGNATURE_SIZE) {
            return false;
        }
        try {
            PublicKey pubKey = reconstructPublicKey(publicKey);
            Signature verifier = Signature.getInstance(ALGORITHM, PROVIDER);
            verifier.initVerify(pubKey);
            verifier.update(message);
            return verifier.verify(signature);
        } catch (Exception e) {
            // Verification failure returns false — never throws for bad signatures
            return false;
        }
    }

    // ── Key reconstruction helpers ──────────────────────────────────────────

    private static PublicKey reconstructPublicKey(byte[] rawPublicKey) throws Exception {
        AlgorithmIdentifier algId = new AlgorithmIdentifier(ML_DSA_44_OID);
        SubjectPublicKeyInfo spki = new SubjectPublicKeyInfo(algId, rawPublicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(spki.getEncoded());
        return KeyFactory.getInstance(ALGORITHM, PROVIDER).generatePublic(spec);
    }

    private static PrivateKey reconstructPrivateKey(byte[] rawSecretKey, byte[] seed)
            throws Exception {
        // BC private key PKCS#8 inner structure: SEQUENCE { seed(32), privateData(2560) }
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new DEROctetString(seed));
        v.add(new DEROctetString(rawSecretKey));
        DERSequence privKeySeq = new DERSequence(v);

        AlgorithmIdentifier algId = new AlgorithmIdentifier(ML_DSA_44_OID);
        PrivateKeyInfo pki = new PrivateKeyInfo(algId, privKeySeq);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(pki.getEncoded());
        return KeyFactory.getInstance(ALGORITHM, PROVIDER).generatePrivate(spec);
    }
}
