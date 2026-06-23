package com.mosscomputing.moss;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class MossCrossLangTest {
    public static void main(String[] args) throws Exception {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        String canonical = "{\"event\":\"agent.action\",\"agent_id\":\"aaaa5555-0000-0000-0000-000000000001\",\"ts\":\"2026-06-05T05:00:00Z\",\"payload\":{\"k\":\"v\"}}";
        byte[] payload = canonical.getBytes(StandardCharsets.UTF_8);
        MossKeyPair kp = MossSigner.generateKeyPair();
        byte[] sig = MossSigner.sign(payload, kp);
        boolean valid = MossSigner.verify(payload, kp.getPublicKey(), sig);
        String outputDir = args.length > 0 ? args[0] : "/tmp/moss-crosslang";
        Files.write(Paths.get(outputDir, "java.pk"), kp.getPublicKey());
        Files.write(Paths.get(outputDir, "java.sig"), sig);
        Files.write(Paths.get(outputDir, "java.sk"), kp.getSecretKey());
        System.out.println("java: pk=" + kp.getPublicKey().length + " sig=" + sig.length + " verified=" + valid);
    }
}
