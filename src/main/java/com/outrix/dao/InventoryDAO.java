package com.outrix.dao;

import com.outrix.config.DBConnection;
import com.outrix.model.InventoryLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO for inventory stock movement logs. */
public class InventoryDAO {

    /** Records a stock movement and updates the product's quantity atomically. */
    public void recordMovement(InventoryLog log) throws SQLException {
        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            // 1. Update product quantity
            String updateQty = "UPDATE products SET quantity=? WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(updateQty)) {
                ps.setInt(1, log.getNewQty()); ps.setInt(2, log.getProductId()); ps.executeUpdate();
            }
            // 2. Insert inventory log
            String insertLog = "INSERT INTO inventory_logs " +
                    "(product_id, user_id, movement_type, quantity, previous_qty, new_qty, reference, notes) " +
                    "VALUES (?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(insertLog)) {
                ps.setInt(1, log.getProductId());
                if (log.getUserId() > 0) ps.setInt(2, log.getUserId()); else ps.setNull(2, Types.INTEGER);
                ps.setString(3, log.getMovementType());
                ps.setInt(4, log.getQuantity());
                ps.setInt(5, log.getPreviousQty());
                ps.setInt(6, log.getNewQty());
                ps.setString(7, log.getReference()); ps.setString(8, log.getNotes());
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback(); throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /** Returns all inventory logs ordered by most recent. */
    public List<InventoryLog> findAll() throws SQLException {
        return query("SELECT il.*, p.product_name, u.username " +
                "FROM inventory_logs il " +
                "LEFT JOIN products p ON il.product_id = p.id " +
                "LEFT JOIN users    u ON il.user_id    = u.id " +
                "ORDER BY il.created_at DESC", null);
    }

    /** Returns inventory logs for a specific product. */
    public List<InventoryLog> findByProduct(int productId) throws SQLException {
        String sql = "SELECT il.*, p.product_name, u.username " +
                "FROM inventory_logs il " +
                "LEFT JOIN products p ON il.product_id = p.id " +
                "LEFT JOIN users    u ON il.user_id    = u.id " +
                "WHERE il.product_id=? ORDER BY il.created_at DESC";
        List<InventoryLog> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private List<InventoryLog> query(String sql, Object param) throws SQLException {
        List<InventoryLog> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) ps.setObject(1, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private InventoryLog mapRow(ResultSet rs) throws SQLException {
        InventoryLog log = new InventoryLog();
        log.setId(rs.getInt("id"));
        log.setProductId(rs.getInt("product_id")); log.setProductName(rs.getString("product_name"));
        int uid = rs.getInt("user_id"); log.setUserId(rs.wasNull() ? 0 : uid);
        log.setUsername(rs.getString("username"));
        log.setMovementType(rs.getString("movement_type")); log.setQuantity(rs.getInt("quantity"));
        log.setPreviousQty(rs.getInt("previous_qty")); log.setNewQty(rs.getInt("new_qty"));
        log.setReference(rs.getString("reference")); log.setNotes(rs.getString("notes"));
        log.setCreatedAt(rs.getTimestamp("created_at"));
        return log;
    }
}
