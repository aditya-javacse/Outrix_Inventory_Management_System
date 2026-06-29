package com.outrix.dao;

import com.outrix.config.DBConnection;
import com.outrix.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO for Supplier entities. */
public class SupplierDAO {

    public List<Supplier> findAll() throws SQLException {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM suppliers ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Supplier> search(String keyword) throws SQLException {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM suppliers WHERE name LIKE ? OR email LIKE ? OR contact_number LIKE ? ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String wc = "%" + keyword + "%";
            ps.setString(1, wc); ps.setString(2, wc); ps.setString(3, wc);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Supplier findById(int id) throws SQLException {
        String sql = "SELECT * FROM suppliers WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public int insert(Supplier s) throws SQLException {
        String sql = "INSERT INTO suppliers (name, contact_number, email, address) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName()); ps.setString(2, s.getContactNumber());
            ps.setString(3, s.getEmail()); ps.setString(4, s.getAddress());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        return -1;
    }

    public void update(Supplier s) throws SQLException {
        String sql = "UPDATE suppliers SET name=?, contact_number=?, email=?, address=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName()); ps.setString(2, s.getContactNumber());
            ps.setString(3, s.getEmail()); ps.setString(4, s.getAddress()); ps.setInt(5, s.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM suppliers WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM suppliers")) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private Supplier mapRow(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setId(rs.getInt("id")); s.setName(rs.getString("name"));
        s.setContactNumber(rs.getString("contact_number"));
        s.setEmail(rs.getString("email")); s.setAddress(rs.getString("address"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        return s;
    }
}
