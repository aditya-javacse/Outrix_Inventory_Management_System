package com.outrix.util;

import com.outrix.model.User;

/**
 * Manages the currently authenticated user session.
 * Thread-safe static session store.
 */
public class SessionManager {

    private static User currentUser;
    private static long loginTime;

    /** Store the authenticated user after successful login. */
    public static void setCurrentUser(User user) {
        currentUser = user;
        loginTime   = System.currentTimeMillis();
    }

    /** Returns the currently logged-in user, or {@code null} if no session. */
    public static User getCurrentUser() {
        return currentUser;
    }

    /** Returns the username of the active session, or empty string. */
    public static String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : "";
    }

    /** Returns the user ID of the active session, or -1. */
    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }

    /** Returns {@code true} if the current user is an ADMIN. */
    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }

    /** Returns {@code true} if a user is currently logged in. */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /** Returns how many milliseconds have elapsed since login. */
    public static long getSessionDuration() {
        return System.currentTimeMillis() - loginTime;
    }

    /** Clears the session (logout). */
    public static void clearSession() {
        currentUser = null;
        loginTime   = 0;
    }
}
