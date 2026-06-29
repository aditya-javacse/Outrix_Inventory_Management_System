package com.outrix.dao;

import com.outrix.config.DBConnection;
import com.outrix.model.User;
import com.outrix.util.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for user accounts.
 */
public class UserDAO {

    /** Find a user by username (case-insensitive). */
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    /** Find a user by ID. */
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    /** Returns all active users. */
    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /**
     * Authenticates a user by username + plain-text password.
     * Returns the User on success, or null on failure.
     */
    public User authenticate(String username, String password) throws SQLException {
        User user = findByUsername(username);
        if (user == null) return null;
        if (!PasswordUtils.verifyPassword(password, user.getPassword())) return null;
        // Update last_login
        updateLastLogin(user.getId());
        return user;
    }

    /** Inserts a new user. Returns the generated ID. */
    public int insert(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, role, email, full_name) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, PasswordUtils.hashPassword(user.getPassword()));
            ps.setString(3, user.getRole());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getFullName());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        return -1;
    }

    /** Updates an existing user (password change included if non-null). */
    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET email=?, full_name=?, role=?, is_active=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getRole());
            ps.setBoolean(4, user.isActive());
            ps.setInt(5, user.getId());
            ps.executeUpdate();
        }
    }

    /** Changes a user's password. */
    public void changePassword(int userId, String newPlainPassword) throws SQLException {
        String sql = "UPDATE users SET password=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, PasswordUtils.hashPassword(newPlainPassword));
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /** Soft-deletes (deactivates) a user. */
    public void delete(int userId) throws SQLException {
        String sql = "UPDATE users SET is_active=0 WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private void updateLastLogin(int userId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET last_login=NOW() WHERE id=?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setRole(rs.getString("role"));
        u.setEmail(rs.getString("email"));
        u.setFullName(rs.getString("full_name"));
        u.setActive(rs.getBoolean("is_active"));
        u.setLastLogin(rs.getTimestamp("last_login"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}
