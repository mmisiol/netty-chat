package org.mmi.chat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHasher {


    public String hashNewPassword(String password) {
        byte[] salt = generateSalt();
        byte[] hash = generateHash(password, salt);
        String encodedSalt = Base64.getEncoder().encodeToString(salt);
        String encodedHash = Base64.getEncoder().encodeToString(hash);
        return encodedSalt + ":" + encodedHash;
    }

    private static byte[] generateSalt() {
        byte[] salt = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return salt;
    }

    private static byte[] generateHash(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hash = md.digest(password.getBytes());
            return hash;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean checkPassword(String password, String storedHash) {
        String[] parts = storedHash.split(":");
        String encodedSalt = parts[0];
        String encodedHash = parts[1];
        byte[] salt = Base64.getDecoder().decode(encodedSalt);
        byte[] expectedHash = Base64.getDecoder().decode(encodedHash);
        byte[] actualHash = generateHash(password, salt);
        return MessageDigest.isEqual(expectedHash, actualHash);
    }
}
