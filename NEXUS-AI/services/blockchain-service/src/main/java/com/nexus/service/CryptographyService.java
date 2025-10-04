package com.nexus.service;

import org.springframework.stereotype.Service;
import java.security.*;
import java.util.Base64;

@Service
public class CryptographyService {
    private final KeyPair keyPair;

    public CryptographyService() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(256);
        this.keyPair = generator.generateKeyPair();
    }

    public String sign(String data) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withECDSA");
        privateSignature.initSign(keyPair.getPrivate());
        privateSignature.update(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        byte[] signature = privateSignature.sign();
        return Base64.getEncoder().encodeToString(signature);
    }

    public String getPublicKey() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }
}