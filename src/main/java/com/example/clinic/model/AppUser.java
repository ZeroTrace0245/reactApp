package com.example.clinic.model;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class AppUser {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ITERATIONS = 65_536;
    private static final int KEY_LENGTH = 256;

    private final UUID id;
    private final String username;
    private final String role;
    private final String passwordHash;
    private final byte[] salt;

    private AppUser(UUID id, String username, String role, String passwordHash, byte[] salt) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.passwordHash = passwordHash;
        this.salt = salt.clone();
    }

    public static AppUser withPassword(String username, String role, String plainPassword) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String hash = hashPassword(plainPassword, salt);
        return new AppUser(UUID.randomUUID(), username, role, hash, salt);
    }

    public static AppUser fromStorage(UUID id, String username, String role, String passwordHash, byte[] salt) {
        return new AppUser(id, username, role, passwordHash, salt);
    }

    public AppUser withNewPassword(String plainPassword) {
        byte[] newSalt = new byte[16];
        RANDOM.nextBytes(newSalt);
        String newHash = hashPassword(plainPassword, newSalt);
        return new AppUser(id, username, role, newHash, newSalt);
    }

    private static String hashPassword(String password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] encoded = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(encoded);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Unable to hash password", e);
        }
    }

    public boolean matches(String candidate) {
        String candidateHash = hashPassword(candidate, salt);
        return Objects.equals(passwordHash, candidateHash);
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public byte[] getSalt() {
        return salt.clone();
    }
}
