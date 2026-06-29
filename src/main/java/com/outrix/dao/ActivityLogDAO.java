package com.outrix.dao;

import com.outrix.config.DBConnection;
import com.outrix.model.ActivityLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO for reading activity audit logs. */
public class ActivityLogDAO {

    public List<ActivityLog> findAll(int limit) throws SQLException {
        List<ActivityLog> list = new ArrayList<>();
        String sql = "SELECT * FROM activity_logs ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<ActivityLog> findByUser(int userId) throws SQLException {
        List<ActivityLog> list = new ArrayList<>();
        String sql = "SELECT * FROM activity_logs WHERE user_id=? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<ActivityLog> search(String keyword) throws SQLException {
        List<ActivityLog> list = new ArrayList<>();
        String sql = "SELECT * FROM activity_logs WHERE username LIKE ? OR action LIKE ? OR description LIKE ? ORDER BY created_at DESC LIMIT 500";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String wc = "%" + keyword + "%";
            ps.setString(1, wc); ps.setString(2, wc); ps.setString(3, wc);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private ActivityLog mapRow(ResultSet rs) throws SQLException {
        ActivityLog log = new ActivityLog();
        log.setId(rs.getInt("id"));
        int uid = rs.getInt("user_id"); log.setUserId(rs.wasNull() ? 0 : uid);
        log.setUsername(rs.getString("username")); log.setAction(rs.getString("action"));
        log.setDescription(rs.getString("description")); log.setIpAddress(rs.getString("ip_address"));
        log.setCreatedAt(rs.getTimestamp("created_at"));
        return log;
    }
}
