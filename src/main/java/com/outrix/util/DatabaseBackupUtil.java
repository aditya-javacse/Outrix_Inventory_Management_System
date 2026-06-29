package com.outrix.util;

import com.outrix.config.DBConnection;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Platform-independent utility for MySQL Database backup and restore operations using JDBC.
 */
public class DatabaseBackupUtil {

    private static final String[] TABLES = {
        "users",
        "categories",
        "suppliers",
        "customers",
        "employees",
        "products",
        "sales",
        "sale_items",
        "inventory_logs",
        "activity_logs"
    };

    /**
     * Dumps the database tables data to a SQL file.
     */
    public static void backup(File file) throws SQLException, IOException {
        try (Connection conn = DBConnection.getConnection();
             BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            writer.write("-- Outrix ERP Database Backup\n");
            writer.write("-- Generated at: " + new java.util.Date() + "\n\n");
            writer.write("SET FOREIGN_KEY_CHECKS = 0;\n\n");

            for (String table : TABLES) {
                writer.write("-- Table: " + table + "\n");
                writer.write("TRUNCATE TABLE " + table + ";\n");

                String selectSql = "SELECT * FROM " + table;
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(selectSql)) {

                    ResultSetMetaData meta = rs.getMetaData();
                    int columnCount = meta.getColumnCount();

                    while (rs.next()) {
                        StringBuilder columns = new StringBuilder();
                        StringBuilder values = new StringBuilder();

                        for (int i = 1; i <= columnCount; i++) {
                            if (i > 1) {
                                columns.append(", ");
                                values.append(", ");
                            }
                            columns.append(meta.getColumnName(i));

                            Object val = rs.getObject(i);
                            if (val == null) {
                                values.append("NULL");
                            } else if (val instanceof Number) {
                                values.append(val);
                            } else if (val instanceof Boolean) {
                                values.append((Boolean) val ? 1 : 0);
                            } else {
                                // String, Date, Timestamp, etc.
                                String strVal = val.toString();
                                // Escape single quotes and backslashes for SQL safety
                                strVal = strVal.replace("\\", "\\\\").replace("'", "\\'");
                                values.append("'").append(strVal).append("'");
                            }
                        }

                        writer.write(String.format("INSERT INTO %s (%s) VALUES (%s);\n",
                                table, columns.toString(), values.toString()));
                    }
                }
                writer.write("\n");
            }

            writer.write("SET FOREIGN_KEY_CHECKS = 1;\n");
            System.out.println("[Backup] Database successfully backed up to: " + file.getAbsolutePath());
        }
    }

    /**
     * Restores the database by executing SQL statements from a file.
     */
    public static void restore(File file) throws SQLException, IOException {
        try (Connection conn = DBConnection.getConnection();
             BufferedReader reader = new BufferedReader(new FileReader(file));
             Statement stmt = conn.createStatement()) {

            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            conn.setAutoCommit(false);
            try {
                while ((line = reader.readLine()) != null) {
                    // Skip comments and empty lines
                    if (line.trim().startsWith("--") || line.trim().isEmpty()) {
                        continue;
                    }
                    sqlBuilder.append(line).append("\n");

                    // If statement ends with a semicolon, execute it
                    if (line.trim().endsWith(";")) {
                        String sql = sqlBuilder.toString().trim();
                        if (!sql.isEmpty()) {
                            stmt.execute(sql);
                        }
                        sqlBuilder.setLength(0); // Reset for next statement
                    }
                }
                conn.commit();
                System.out.println("[Restore] Database successfully restored from: " + file.getAbsolutePath());
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
