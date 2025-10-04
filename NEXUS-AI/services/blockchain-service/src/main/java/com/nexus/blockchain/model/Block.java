package com.nexus.blockchain.model;

import lombok.Getter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

@Getter
public class Block {
    private final String hash;
    private final String previousHash;
    private final String data;
    private final long timeStamp;
    private final String validator;// Address of the validator
    private final String signature; // Signature of the block hash by the validator

    public Block(String data, String previousHash,String validator, String signature) {
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.validator = validator;
        this.signature = signature;
        this.hash = calculateHash();

    }

    public String calculateHash() {
        String dataToHash = previousHash + timeStamp + data + validator;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
