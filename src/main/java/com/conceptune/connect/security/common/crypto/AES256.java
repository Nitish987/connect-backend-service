package com.conceptune.connect.security.common.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@Component
public class AES256 {
    private static final byte[] SALTED = "Salted__".getBytes(StandardCharsets.UTF_8);

    @Value("${connect.security.aes.passphrase}")
    private String passphrase;

    public String encrypt(String raw) throws Exception {
        return Base64.getEncoder().encodeToString(_encrypt(raw.getBytes(StandardCharsets.UTF_8), passphrase.getBytes(StandardCharsets.UTF_8)));
    }

    public String decrypt(String cipher) throws Exception {
        return new String(_decrypt(Base64.getDecoder().decode(cipher), passphrase.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    public String encrypt(String raw, String passphrase) throws Exception {
        return Base64.getEncoder().encodeToString(_encrypt(raw.getBytes(StandardCharsets.UTF_8), passphrase.getBytes(StandardCharsets.UTF_8)));
    }

    public String decrypt(String cipher, String passphrase) throws Exception {
        return new String(_decrypt(Base64.getDecoder().decode(cipher), passphrase.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    private byte[] _encrypt(byte[] input, byte[] passphrase) throws Exception {
        byte[] salt = (new SecureRandom()).generateSeed(8);
        Object[] keyIv = deriveKeyAndIv(passphrase, salt);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec((byte[]) keyIv[0], "AES"), new IvParameterSpec((byte[]) keyIv[1]));

        byte[] enc = cipher.doFinal(input);
        return concat(concat(SALTED, salt), enc);
    }

    private byte[] _decrypt(byte[] data, byte[] passphrase) throws Exception {
        byte[] salt = Arrays.copyOfRange(data, 8, 16);

        if (!Arrays.equals(Arrays.copyOfRange(data, 0, 8), SALTED)) {
            throw new IllegalArgumentException("Invalid crypted data");
        }

        Object[] keyIv = deriveKeyAndIv(passphrase, salt);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec((byte[]) keyIv[0], "AES"), new IvParameterSpec((byte[]) keyIv[1]));
        return cipher.doFinal(data, 16, data.length - 16);
    }

    private Object[] deriveKeyAndIv(byte[] passphrase, byte[] salt) throws Exception {
        final MessageDigest md5 = MessageDigest.getInstance("MD5");
        final byte[] passSalt = concat(passphrase, salt);
        byte[] dx = new byte[0];
        byte[] di = new byte[0];

        for (int i = 0; i < 3; i++) {
            di = md5.digest(concat(di, passSalt));
            dx = concat(dx, di);
        }

        return new Object[]{Arrays.copyOfRange(dx, 0, 32), Arrays.copyOfRange(dx, 32, 48)};
    }

    private byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
}
