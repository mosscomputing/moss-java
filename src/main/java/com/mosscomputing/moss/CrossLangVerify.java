package com.mosscomputing.moss;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;

public class CrossLangVerify {
    public static void main(String[] args) throws Exception {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        byte[] canonical = "{\"event\":\"agent.action\",\"agent_id\":\"aaaa5555-0000-0000-0000-000000000001\",\"ts\":\"2026-06-05T05:00:00Z\",\"payload\":{\"k\":\"v\"}}".getBytes(StandardCharsets.UTF_8);
        String[] signers = {"ts", "go", "java", "dotnet", "python"};
        String baseDir = "/tmp/moss-crosslang";
        for (String signer : signers) {
            byte[] pk = Files.readAllBytes(Paths.get(baseDir, signer + ".pk"));
            byte[] sig = Files.readAllBytes(Paths.get(baseDir, signer + ".sig"));
            boolean valid = MossSigner.verify(canonical, pk, sig);
            System.out.println(signer + " signed -> java verified: " + (valid ? "PASS" : "FAIL"));
        }
    }
}
