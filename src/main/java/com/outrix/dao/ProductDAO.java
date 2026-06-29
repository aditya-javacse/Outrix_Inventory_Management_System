package com.outrix.dao;

import com.outrix.config.DBConnection;
import com.outrix.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Product entities.
 */
public class ProductDAO {

    /** Returns all products with category and supplier names joined. */
    public List<Product> findAll() throws SQLException {
        return query("SELECT p.*, c.name AS category_name, s.name AS supplier_name " +
                     "FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id " +
                     "LEFT JOIN suppliers s  ON p.supplier_id  = s.id " +
                     "ORDER BY p.product_name", null);
    }

    /** Searches products by name or barcode. */
    public List<Product> search(String keyword) throws SQLException {
        String q = "SELECT p.*, c.name AS category_name, s.name AS supplier_name " +
                   "FROM products p " +
                   "LEFT JOIN categories c ON p.category_id = c.id " +
                   "LEFT JOIN suppliers s  ON p.supplier_id  = s.id " +
                   "WHERE p.product_name LIKE ? OR p.barcode LIKE ? " +
                   "ORDER BY p.product_name";
        return query(q, "%" + keyword + "%");
    }

    /** Returns all low-stock and out-of-stock products. */
    public List<Product> findLowStock() throws SQLException {
        String q = "SELECT p.*, c.name AS category_name, s.name AS supplier_name " +
                   "FROM products p " +
                   "LEFT JOIN categories c ON p.category_id = c.id " +
                   "LEFT JOIN suppliers s  ON p.supplier_id  = s.id " +
                   "WHERE p.quantity <= p.low_stock_threshold " +
                   "ORDER BY p.quantity";
        return query(q, null);
    }

    /** Returns all products filtered by category. */
    public List<Product> findByCategory(int categoryId) throws SQLException {
        String q = "SELECT p.*, c.name AS category_name, s.name AS supplier_name " +
                   "FROM products p " +
                   "LEFT JOIN categories c ON p.category_id = c.id " +
                   "LEFT JOIN suppliers s  ON p.supplier_id  = s.id " +
                   "WHERE p.category_id = ? ORDER BY p.product_name";
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, categoryId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** Finds a product by ID. */
    public Product findById(int id) throws SQLException {
        String q = "SELECT p.*, c.name AS category_name, s.name AS supplier_name " +
                   "FROM products p " +
                   "LEFT JOIN categories c ON p.category_id = c.id " +
                   "LEFT JOIN suppliers s  ON p.supplier_id  = s.id " +
                   "WHERE p.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    /** Finds a product by barcode. */
    public Product findByBarcode(String barcode) throws SQLException {
        String q = "SELECT p.*, c.name AS category_name, s.name AS supplier_name " +
                   "FROM products p " +
                   "LEFT JOIN categories c ON p.category_id = c.id " +
                   "LEFT JOIN suppliers s  ON p.supplier_id  = s.id " +
                   "WHERE p.barcode = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setString(1, barcode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    /** Inserts a new product. Returns the generated ID. */
    public int insert(Product p) throws SQLException {
        String sql = "INSERT INTO products (product_name, category_id, supplier_id, description, " +
                     "purchase_price, selling_price, quantity, low_stock_threshold, barcode) " +
                     "VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getProductName());
            ps.setInt(2, p.getCategoryId());
            if (p.getSupplierId() != null) ps.setInt(3, p.getSupplierId()); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, p.getDescription());
            ps.setBigDecimal(5, p.getPurchasePrice());
            ps.setBigDecimal(6, p.getSellingPrice());
            ps.setInt(7, p.getQuantity());
            ps.setInt(8, p.getLowStockThreshold());
            ps.setString(9, p.getBarcode());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        return -1;
    }

    /** Updates an existing product. */
    public void update(Product p) throws SQLException {
        String sql = "UPDATE products SET product_name=?, category_id=?, supplier_id=?, description=?, " +
                     "purchase_price=?, selling_price=?, quantity=?, low_stock_threshold=?, barcode=? " +
                     "WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getProductName());
            ps.setInt(2, p.getCategoryId());
            if (p.getSupplierId() != null) ps.setInt(3, p.getSupplierId()); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, p.getDescription());
            ps.setBigDecimal(5, p.getPurchasePrice());
            ps.setBigDecimal(6, p.getSellingPrice());
            ps.setInt(7, p.getQuantity());
            ps.setInt(8, p.getLowStockThreshold());
            ps.setString(9, p.getBarcode());
            ps.setInt(10, p.getId());
            ps.executeUpdate();
        }
    }

    /** Updates only the quantity field (used by inventory and sales DAOs). */
    public void updateQuantity(int productId, int newQuantity) throws SQLException {
        String sql = "UPDATE products SET quantity=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    /** Deletes a product by ID. */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Returns total product count. */
    public int count() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM products")) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private List<Product> query(String sql, String wildcardParam) throws SQLException {
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (wildcardParam != null) {
                ps.setString(1, wildcardParam);
                ps.setString(2, wildcardParam);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setProductName(rs.getString("product_name"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        int sid = rs.getInt("supplier_id");
        p.setSupplierId(rs.wasNull() ? null : sid);
        p.setSupplierName(rs.getString("supplier_name"));
        p.setDescription(rs.getString("description"));
        p.setPurchasePrice(rs.getBigDecimal("purchase_price"));
        p.setSellingPrice(rs.getBigDecimal("selling_price"));
        p.setQuantity(rs.getInt("quantity"));
        p.setLowStockThreshold(rs.getInt("low_stock_threshold"));
        p.setBarcode(rs.getString("barcode"));
        p.setDateAdded(rs.getTimestamp("date_added"));
        p.setUpdatedAt(rs.getTimestamp("updated_at"));
        return p;
    }
}
