package com.outrix.dao;

import com.outrix.config.DBConnection;
import com.outrix.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO for Customer entities. */
public class CustomerDAO {

    public List<Customer> findAll() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Customer> search(String keyword) throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE name LIKE ? OR phone LIKE ? OR email LIKE ? ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String wc = "%" + keyword + "%";
            ps.setString(1, wc); ps.setString(2, wc); ps.setString(3, wc);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Customer findById(int id) throws SQLException {
        String sql = "SELECT * FROM customers WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public int insert(Customer c) throws SQLException {
        String sql = "INSERT INTO customers (name, phone, email, address) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName()); ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail()); ps.setString(4, c.getAddress());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        return -1;
    }

    public void update(Customer c) throws SQLException {
        String sql = "UPDATE customers SET name=?, phone=?, email=?, address=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName()); ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail()); ps.setString(4, c.getAddress());
            ps.setInt(5, c.getId()); ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM customers WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM customers")) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("id")); c.setName(rs.getString("name"));
        c.setPhone(rs.getString("phone")); c.setEmail(rs.getString("email"));
        c.setAddress(rs.getString("address")); c.setCreatedAt(rs.getTimestamp("created_at"));
        return c;
    }
}
