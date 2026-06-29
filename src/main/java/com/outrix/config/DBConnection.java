package com.outrix.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton database connection manager.
 * Provides a single shared JDBC connection to the MySQL database.
 */
public class DBConnection {

    // ── Connection parameters ─────────────────────────────────────────────────
    private static final String HOST     = "localhost";
    private static final String PORT     = "3306";
    private static final String DATABASE = "outrix_erp";
    private static final String USER     = "root";
    private static final String PASSWORD = "04062005aditya";

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    // ── Singleton instance ────────────────────────────────────────────────────
    private static Connection instance;

    /** Private constructor – use {@link #getConnection()} instead. */
    private DBConnection() {}

    /**
     * Returns the shared Connection, creating one if it is null or closed.
     *
     * @return a live {@link Connection}
     * @throws SQLException if a database access error occurs
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (instance == null || instance.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                instance = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("[DB] Connected to " + DATABASE + " on " + HOST + ":" + PORT);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found.", e);
            }
        }
        return instance;
    }

    /**
     * Closes the connection if it is open.
     * Should be called only on application shutdown.
     */
    public static synchronized void closeConnection() {
        if (instance != null) {
            try {
                if (!instance.isClosed()) {
                    instance.close();
                    System.out.println("[DB] Connection closed.");
                }
            } catch (SQLException e) {
                System.err.println("[DB] Error closing connection: " + e.getMessage());
            } finally {
                instance = null;
            }
        }
    }

    /**
     * Quick connectivity test. Returns {@code true} if the DB can be reached.
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[DB] Connection test failed: " + e.getMessage());
            return false;
        }
    }
}
