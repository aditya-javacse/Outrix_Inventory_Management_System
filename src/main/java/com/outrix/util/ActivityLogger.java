package com.outrix.util;

import com.outrix.config.DBConnection;
import com.outrix.util.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Utility for writing activity audit logs to the database.
 */
public class ActivityLogger {

    private ActivityLogger() {}

    /**
     * Logs an action by the currently logged-in user.
     *
     * @param action      short action code (e.g. "LOGIN", "ADD_PRODUCT")
     * @param description human-readable description
     */
    public static void log(String action, String description) {
        int    userId   = SessionManager.getCurrentUserId();
        String username = SessionManager.getCurrentUsername();
        log(userId, username, action, description);
    }

    /**
     * Logs an action with explicit user details (useful for login events
     * before a session is established).
     */
    public static void log(int userId, String username, String action, String description) {
        String sql = "INSERT INTO activity_logs (user_id, username, action, description) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, username);
            ps.setString(3, action);
            ps.setString(4, description);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ActivityLogger] Failed to write log: " + e.getMessage());
        }
    }
}
