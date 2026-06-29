package com.outrix.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and verification using BCrypt.
 */
public class PasswordUtils {

    private static final int BCRYPT_ROUNDS = 12;

    /** Private constructor – static utility class. */
    private PasswordUtils() {}

    /**
     * Hashes a plain-text password using BCrypt.
     *
     * @param plainText the raw password
     * @return BCrypt hash string
     */
    public static String hashPassword(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Verifies a plain-text password against a stored BCrypt hash.
     *
     * @param plainText the raw password candidate
     * @param hashed    the stored BCrypt hash
     * @return {@code true} if the password matches
     */
    public static boolean verifyPassword(String plainText, String hashed) {
        if (plainText == null || hashed == null || hashed.isEmpty()) return false;
        try {
            return BCrypt.checkpw(plainText, hashed);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates password strength: at least 8 chars, 1 uppercase, 1 digit.
     *
     * @param password the candidate password
     * @return {@code true} if strong enough
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper  = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit  = password.chars().anyMatch(Character::isDigit);
        return hasUpper && hasDigit;
    }
}
