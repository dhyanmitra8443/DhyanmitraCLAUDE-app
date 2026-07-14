package com.lms.settings;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Ref: SRS 16.9, 17.24 - the Razorpay key secret, Razorpay webhook secret
 * and SMTP password are administrator-supplied credentials that live in the
 * database, so they must be encrypted at rest rather than stored in the
 * clear (the schema's *_encrypted column names are a contract, not a hint).
 *
 * AES-256-GCM: authenticated encryption, so a tampered ciphertext fails to
 * decrypt rather than silently yielding garbage. A fresh random IV is
 * generated per encryption and prepended to the ciphertext, which is why
 * encrypting the same secret twice produces different output - that is
 * correct and required; a fixed IV under a fixed key is catastrophic for GCM.
 *
 * The key is derived by SHA-256 over the configured passphrase, so any
 * passphrase length yields a valid 256-bit key. Rotating
 * SETTINGS_ENCRYPTION_KEY makes previously stored secrets undecryptable -
 * an administrator must re-enter them (see SettingsProperties).
 */
@Service
public class SecretCipher {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;      // GCM standard
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public SecretCipher(SettingsProperties properties) {
        this.key = new SecretKeySpec(sha256(properties.encryptionKey()), "AES");
    }

    /** Returns Base64(iv || ciphertext || tag), or null for null/blank input. */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isBlank()) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to encrypt setting value", e);
        }
    }

    /**
     * Reverses encrypt(). Returns null when the stored value cannot be
     * decrypted (wrong/rotated key, or tampered ciphertext) rather than
     * throwing, so one unreadable credential can't take down every caller
     * that merely reads settings - callers treat null as "not configured"
     * and fall back to environment configuration.
     */
    public String decrypt(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return null;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(encoded);
            if (combined.length <= IV_LENGTH_BYTES) {
                return null;
            }
            byte[] iv = new byte[IV_LENGTH_BYTES];
            byte[] ciphertext = new byte[combined.length - IV_LENGTH_BYTES];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTES);
            System.arraycopy(combined, IV_LENGTH_BYTES, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            return null;
        }
    }

    private static byte[] sha256(String passphrase) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(passphrase.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
