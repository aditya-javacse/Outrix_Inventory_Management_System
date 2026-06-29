package com.outrix.dao;

import com.outrix.config.DBConnection;
import com.outrix.model.Sale;
import com.outrix.model.SaleItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO for Sale invoices and their line items. */
public class SaleDAO {

    /**
     * Saves a complete sale (header + items) in a single transaction.
     * Also decrements product quantities.
     */
    public int saveSale(Sale sale) throws SQLException {
        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            // Insert sale header
            String headerSql = "INSERT INTO sales (invoice_number, customer_id, user_id, " +
                    "total_amount, discount, tax, grand_total, payment_method, notes) " +
                    "VALUES (?,?,?,?,?,?,?,?,?)";
            int saleId;
            try (PreparedStatement ps = conn.prepareStatement(headerSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, sale.getInvoiceNumber());
                if (sale.getCustomerId() != null) ps.setInt(2, sale.getCustomerId()); else ps.setNull(2, Types.INTEGER);
                if (sale.getUserId()     != null) ps.setInt(3, sale.getUserId());     else ps.setNull(3, Types.INTEGER);
                ps.setBigDecimal(4, sale.getTotalAmount()); ps.setBigDecimal(5, sale.getDiscount());
                ps.setBigDecimal(6, sale.getTax()); ps.setBigDecimal(7, sale.getGrandTotal());
                ps.setString(8, sale.getPaymentMethod()); ps.setString(9, sale.getNotes());
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys(); keys.next(); saleId = keys.getInt(1);
            }
            // Insert line items + decrement quantities
            String itemSql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, subtotal) VALUES (?,?,?,?,?)";
            String decrSql  = "UPDATE products SET quantity = quantity - ? WHERE id=?";
            try (PreparedStatement itemPs = conn.prepareStatement(itemSql);
                 PreparedStatement decrPs = conn.prepareStatement(decrSql)) {
                for (SaleItem item : sale.getItems()) {
                    itemPs.setInt(1, saleId); itemPs.setInt(2, item.getProductId());
                    itemPs.setInt(3, item.getQuantity()); itemPs.setBigDecimal(4, item.getUnitPrice());
                    itemPs.setBigDecimal(5, item.getSubtotal()); itemPs.addBatch();

                    decrPs.setInt(1, item.getQuantity()); decrPs.setInt(2, item.getProductId()); decrPs.addBatch();
                }
                itemPs.executeBatch(); decrPs.executeBatch();
            }
            conn.commit();
            return saleId;
        } catch (SQLException e) {
            conn.rollback(); throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /** Returns all sales with customer and user names. */
    public List<Sale> findAll() throws SQLException {
        String sql = "SELECT s.*, c.name AS customer_name, u.username " +
                "FROM sales s LEFT JOIN customers c ON s.customer_id=c.id " +
                "LEFT JOIN users u ON s.user_id=u.id ORDER BY s.sale_date DESC";
        return querySales(sql);
    }

    /** Returns sales for a date range (YYYY-MM-DD strings). */
    public List<Sale> findByDateRange(String from, String to) throws SQLException {
        String sql = "SELECT s.*, c.name AS customer_name, u.username " +
                "FROM sales s LEFT JOIN customers c ON s.customer_id=c.id " +
                "LEFT JOIN users u ON s.user_id=u.id " +
                "WHERE DATE(s.sale_date) BETWEEN ? AND ? ORDER BY s.sale_date DESC";
        List<Sale> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from); ps.setString(2, to);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapSaleRow(rs));
        }
        return list;
    }

    /** Returns a single sale with its items loaded. */
    public Sale findById(int id) throws SQLException {
        String sql = "SELECT s.*, c.name AS customer_name, u.username " +
                "FROM sales s LEFT JOIN customers c ON s.customer_id=c.id " +
                "LEFT JOIN users u ON s.user_id=u.id WHERE s.id=?";
        Sale sale = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id); ResultSet rs = ps.executeQuery();
            if (rs.next()) sale = mapSaleRow(rs);
        }
        if (sale != null) sale.setItems(findItemsBySaleId(id));
        return sale;
    }

    /** Returns all line items for a sale. */
    public List<SaleItem> findItemsBySaleId(int saleId) throws SQLException {
        List<SaleItem> items = new ArrayList<>();
        String sql = "SELECT si.*, p.product_name FROM sale_items si " +
                "LEFT JOIN products p ON si.product_id=p.id WHERE si.sale_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saleId); ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                SaleItem item = new SaleItem();
                item.setId(rs.getInt("id")); item.setSaleId(saleId);
                item.setProductId(rs.getInt("product_id")); item.setProductName(rs.getString("product_name"));
                item.setQuantity(rs.getInt("quantity")); item.setUnitPrice(rs.getBigDecimal("unit_price"));
                item.setSubtotal(rs.getBigDecimal("subtotal")); items.add(item);
            }
        }
        return items;
    }

    /** Total revenue for today. */
    public BigDecimal getTodayRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(grand_total),0) FROM sales WHERE DATE(sale_date)=CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getBigDecimal(1);
        }
        return BigDecimal.ZERO;
    }

    /** Monthly revenue for the last 12 months (label → amount). */
    public List<Object[]> getMonthlyRevenue() throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(sale_date,'%b %Y') AS month, SUM(grand_total) AS revenue " +
                "FROM sales WHERE sale_date >= DATE_SUB(NOW(), INTERVAL 12 MONTH) " +
                "GROUP BY YEAR(sale_date), MONTH(sale_date) ORDER BY YEAR(sale_date), MONTH(sale_date)";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) rows.add(new Object[]{rs.getString("month"), rs.getBigDecimal("revenue")});
        }
        return rows;
    }

    /** Top-selling products (name, total sold). */
    public List<Object[]> getTopProducts(int limit) throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT p.product_name, SUM(si.quantity) AS total_sold " +
                "FROM sale_items si JOIN products p ON si.product_id=p.id " +
                "GROUP BY si.product_id ORDER BY total_sold DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit); ResultSet rs = ps.executeQuery();
            while (rs.next()) rows.add(new Object[]{rs.getString("product_name"), rs.getInt("total_sold")});
        }
        return rows;
    }

    public int count() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM sales")) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /** Generates next invoice number in format INV-YYYYMMDD-NNNN. */
    public static String generateInvoiceNumber() throws SQLException {
        String date = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        String sql  = "SELECT COUNT(*)+1 FROM sales WHERE DATE(sale_date)=CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            int seq = rs.next() ? rs.getInt(1) : 1;
            return String.format("INV-%s-%04d", date, seq);
        }
    }

    private List<Sale> querySales(String sql) throws SQLException {
        List<Sale> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapSaleRow(rs));
        }
        return list;
    }

    private Sale mapSaleRow(ResultSet rs) throws SQLException {
        Sale s = new Sale();
        s.setId(rs.getInt("id")); s.setInvoiceNumber(rs.getString("invoice_number"));
        int cid = rs.getInt("customer_id"); s.setCustomerId(rs.wasNull() ? null : cid);
        s.setCustomerName(rs.getString("customer_name"));
        int uid = rs.getInt("user_id"); s.setUserId(rs.wasNull() ? null : uid);
        s.setUsername(rs.getString("username"));
        s.setTotalAmount(rs.getBigDecimal("total_amount")); s.setDiscount(rs.getBigDecimal("discount"));
        s.setTax(rs.getBigDecimal("tax")); s.setGrandTotal(rs.getBigDecimal("grand_total"));
        s.setPaymentMethod(rs.getString("payment_method")); s.setNotes(rs.getString("notes"));
        s.setSaleDate(rs.getTimestamp("sale_date"));
        return s;
    }
}
