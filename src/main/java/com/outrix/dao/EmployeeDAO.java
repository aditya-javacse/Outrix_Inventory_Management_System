package com.outrix.dao;

import com.outrix.config.DBConnection;
import com.outrix.model.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO for Employee entities (joined with users table). */
public class EmployeeDAO {

    private static final String BASE_SELECT =
        "SELECT e.*, u.username, u.role AS user_role, u.email AS user_email " +
        "FROM employees e LEFT JOIN users u ON e.user_id = u.id ";

    public List<Employee> findAll() throws SQLException {
        List<Employee> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(BASE_SELECT + "ORDER BY e.name")) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Employee> search(String keyword) throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE e.name LIKE ? OR e.email LIKE ? OR u.username LIKE ? ORDER BY e.name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String wc = "%" + keyword + "%";
            ps.setString(1, wc); ps.setString(2, wc); ps.setString(3, wc);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Employee findById(int id) throws SQLException {
        String sql = BASE_SELECT + "WHERE e.id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public int insert(Employee e) throws SQLException {
        String sql = "INSERT INTO employees (user_id, name, email, phone, role, hire_date) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (e.getUserId() > 0) ps.setInt(1, e.getUserId()); else ps.setNull(1, Types.INTEGER);
            ps.setString(2, e.getName()); ps.setString(3, e.getEmail());
            ps.setString(4, e.getPhone()); ps.setString(5, e.getRole());
            ps.setDate(6, e.getHireDate());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        return -1;
    }

    public void update(Employee e) throws SQLException {
        String sql = "UPDATE employees SET name=?, email=?, phone=?, role=?, hire_date=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getName()); ps.setString(2, e.getEmail());
            ps.setString(3, e.getPhone()); ps.setString(4, e.getRole());
            ps.setDate(5, e.getHireDate()); ps.setInt(6, e.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM employees WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM employees")) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private Employee mapRow(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setId(rs.getInt("id"));
        int uid = rs.getInt("user_id"); e.setUserId(rs.wasNull() ? 0 : uid);
        e.setName(rs.getString("name")); e.setEmail(rs.getString("email"));
        e.setPhone(rs.getString("phone")); e.setRole(rs.getString("role"));
        e.setHireDate(rs.getDate("hire_date")); e.setCreatedAt(rs.getTimestamp("created_at"));
        e.setUsername(rs.getString("username")); e.setUserRole(rs.getString("user_role"));
        return e;
    }
}
