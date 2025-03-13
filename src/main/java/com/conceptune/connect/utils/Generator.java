package com.conceptune.connect.utils;

import java.security.SecureRandom;

public class Generator {

    public static final String ALLOWED_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{}|;:,.<>?";
    public static final String ALLOWED_TOKEN_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String generateString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(ALLOWED_CHARACTERS.length());
            sb.append(ALLOWED_CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    public static String generateString(int length, String prefix, String suffix) {
        return prefix + generateString(length) + suffix;
    }

    public static String generateToken(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(ALLOWED_TOKEN_CHARACTERS.length());
            sb.append(ALLOWED_TOKEN_CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    public static String generateOneTimePassword() {
        return generateNumberString(6);
    }

    public static String generateNumberString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(10);
            sb.append(index);
        }
        return sb.toString();
    }

    public static String generateUsername(String name) {
        return String.join("", name.toLowerCase().split(" ")) + generateNumberString(5);
    }
}
