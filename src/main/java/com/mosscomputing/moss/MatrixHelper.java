package com.mosscomputing.moss;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * Cross-language matrix helper for Java SDK.
 *
 * Usage:
 *   java com.mosscomputing.moss.MatrixHelper sign <output_dir>
 *   java com.mosscomputing.moss.MatrixHelper verify <output_dir> <signer>
 *   java com.mosscomputing.moss.MatrixHelper verify-tamper <output_dir> <signer>
 */
public class MatrixHelper {

    private static final byte[] CANONICAL_PAYLOAD =
        "{\"event\":\"agent.action\",\"agent_id\":\"a-1\",\"ts\":\"2026-06-03T00:00:00Z\",\"payload\":{\"k\":\"v\"}}"
            .getBytes(StandardCharsets.UTF_8);

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: MatrixHelper <sign|verify|verify-tamper> <output_dir> [signer]");
            System.exit(1);
        }

        String cmd = args[0];
        String outputDir = args[1];

        switch (cmd) {
            case "sign":
                doSign(outputDir);
                break;
            case "verify":
                if (args.length < 3) {
                    System.err.println("Usage: MatrixHelper verify <output_dir> <signer>");
                    System.exit(1);
                }
                doVerify(outputDir, args[2]);
                break;
            case "verify-tamper":
                if (args.length < 3) {
                    System.err.println("Usage: MatrixHelper verify-tamper <output_dir> <signer>");
                    System.exit(1);
                }
                doVerifyTamper(outputDir, args[2]);
                break;
            default:
                System.err.println("Unknown command: " + cmd);
                System.exit(1);
        }
    }

    private static void doSign(String outputDir) throws Exception {
        MossKeyPair kp = MossSigner.generateKeyPair();
        byte[] sig = MossSigner.sign(CANONICAL_PAYLOAD, kp);

        Files.write(Paths.get(outputDir, "java.pk"), kp.getPublicKey());
        Files.write(Paths.get(outputDir, "java.sig"), sig);

        System.out.println("java: pk=" + kp.getPublicKey().length + " sig=" + sig.length + " written");
    }

    private static void doVerify(String outputDir, String signer) throws Exception {
        byte[] pk = Files.readAllBytes(Paths.get(outputDir, signer + ".pk"));
        byte[] sig = Files.readAllBytes(Paths.get(outputDir, signer + ".sig"));

        boolean valid = MossSigner.verify(CANONICAL_PAYLOAD, pk, sig);
        System.out.println(valid ? "PASS" : "FAIL");
    }

    private static void doVerifyTamper(String outputDir, String signer) throws Exception {
        byte[] pk = Files.readAllBytes(Paths.get(outputDir, signer + ".pk"));
        byte[] sig = Files.readAllBytes(Paths.get(outputDir, signer + ".sig"));

        // Flip one byte
        sig[0] ^= 0x01;

        boolean valid = MossSigner.verify(CANONICAL_PAYLOAD, pk, sig);
        System.out.println(!valid ? "REJECTED" : "ACCEPTED");
    }
}
