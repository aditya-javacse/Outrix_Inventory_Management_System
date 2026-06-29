-- ============================================================
-- Outrix ERP Inventory Management System
-- Database Schema v1.0
-- ============================================================

CREATE DATABASE IF NOT EXISTS outrix_erp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE outrix_erp;

-- ============================================================
-- TABLE: categories
-- ============================================================
CREATE TABLE IF NOT EXISTS categories (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE: suppliers
-- ============================================================
CREATE TABLE IF NOT EXISTS suppliers (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(150) NOT NULL,
    contact_number VARCHAR(20),
    email          VARCHAR(100),
    address        TEXT,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE: products
-- ============================================================
CREATE TABLE IF NOT EXISTS products (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    product_name   VARCHAR(200) NOT NULL,
    category_id    INT NOT NULL,
    supplier_id    INT,
    description    TEXT,
    purchase_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    selling_price  DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    quantity       INT NOT NULL DEFAULT 0,
    low_stock_threshold INT NOT NULL DEFAULT 10,
    barcode        VARCHAR(100) UNIQUE,
    date_added     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL
);

-- ============================================================
-- TABLE: users (login credentials)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    role         ENUM('ADMIN','EMPLOYEE') NOT NULL DEFAULT 'EMPLOYEE',
    email        VARCHAR(100),
    full_name    VARCHAR(150),
    is_active    TINYINT(1) NOT NULL DEFAULT 1,
    last_login   TIMESTAMP NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE: employees
-- ============================================================
CREATE TABLE IF NOT EXISTS employees (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT UNIQUE,
    name         VARCHAR(150) NOT NULL,
    email        VARCHAR(100),
    phone        VARCHAR(20),
    role         VARCHAR(100),
    hire_date    DATE,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================================
-- TABLE: customers
-- ============================================================
CREATE TABLE IF NOT EXISTS customers (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(150) NOT NULL,
    phone      VARCHAR(20),
    email      VARCHAR(100),
    address    TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE: sales (invoice header)
-- ============================================================
CREATE TABLE IF NOT EXISTS sales (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id   INT,
    user_id       INT,
    total_amount  DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    discount      DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax           DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    grand_total   DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    payment_method ENUM('CASH','CARD','MOBILE','OTHER') DEFAULT 'CASH',
    notes         TEXT,
    sale_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id)     REFERENCES users(id)     ON DELETE SET NULL
);

-- ============================================================
-- TABLE: sale_items (invoice line items)
-- ============================================================
CREATE TABLE IF NOT EXISTS sale_items (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    sale_id    INT NOT NULL,
    product_id INT NOT NULL,
    quantity   INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    subtotal   DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (sale_id)    REFERENCES sales(id)    ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
);

-- ============================================================
-- TABLE: inventory_logs (stock movements)
-- ============================================================
CREATE TABLE IF NOT EXISTS inventory_logs (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    product_id    INT NOT NULL,
    user_id       INT,
    movement_type ENUM('STOCK_IN','STOCK_OUT','ADJUSTMENT','TRANSFER') NOT NULL,
    quantity      INT NOT NULL,
    previous_qty  INT NOT NULL DEFAULT 0,
    new_qty       INT NOT NULL DEFAULT 0,
    reference     VARCHAR(200),
    notes         TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE SET NULL
);

-- ============================================================
-- TABLE: activity_logs (audit trail)
-- ============================================================
CREATE TABLE IF NOT EXISTS activity_logs (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT,
    username    VARCHAR(50),
    action      VARCHAR(100) NOT NULL,
    description TEXT,
    ip_address  VARCHAR(45),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================================
-- INDEXES for performance
-- ============================================================
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_supplier ON products(supplier_id);
CREATE INDEX idx_products_barcode  ON products(barcode);
CREATE INDEX idx_sales_date        ON sales(sale_date);
CREATE INDEX idx_sales_customer    ON sales(customer_id);
CREATE INDEX idx_sale_items_sale   ON sale_items(sale_id);
CREATE INDEX idx_inv_logs_product  ON inventory_logs(product_id);
CREATE INDEX idx_activity_user     ON activity_logs(user_id);
CREATE INDEX idx_activity_date     ON activity_logs(created_at);

-- ============================================================
-- SEED DATA
-- ============================================================

-- Default admin user (password: Admin@123)
-- BCrypt hash generated for "Admin@123"
INSERT INTO users (username, password, role, email, full_name) VALUES
('admin', '$2a$12$EhDA2Xq1aF7KaEbRY.ERre8pvgDqef3QU.9zYSohFYlX27hvwlipq', 'ADMIN', 'admin@outrix.com', 'System Administrator'),
('employee1', '$2a$12$EhDA2Xq1aF7KaEbRY.ERre8pvgDqef3QU.9zYSohFYlX27hvwlipq', 'EMPLOYEE', 'emp1@outrix.com', 'John Smith');

-- Default employee record
INSERT INTO employees (user_id, name, email, phone, role, hire_date) VALUES
(2, 'John Smith', 'emp1@outrix.com', '555-0100', 'Sales Associate', CURDATE());

-- Default categories
INSERT INTO categories (name, description) VALUES
('Electronics',   'Electronic devices and accessories'),
('Clothing',      'Apparel and fashion items'),
('Food & Beverage','Consumable food and drink products'),
('Office Supplies','Stationery and office equipment'),
('Hardware',      'Tools and hardware items'),
('Software',      'Software licenses and media');

-- Default suppliers
INSERT INTO suppliers (name, contact_number, email, address) VALUES
('TechSource Inc.',    '555-1000', 'orders@techsource.com',   '100 Tech Park, Silicon Valley, CA'),
('Fashion World Ltd.', '555-2000', 'supply@fashionworld.com', '200 Garment St, New York, NY'),
('OfficeMax Supplies', '555-3000', 'bulk@officemax.com',      '300 Office Blvd, Chicago, IL');

-- Sample products
INSERT INTO products (product_name, category_id, supplier_id, description, purchase_price, selling_price, quantity, low_stock_threshold, barcode) VALUES
('Wireless Mouse',         1, 1, 'Ergonomic wireless mouse with 2.4GHz connectivity', 12.50, 29.99, 150, 20, 'PRD-1001'),
('Mechanical Keyboard',    1, 1, 'Full-size mechanical keyboard with RGB backlight',  45.00, 89.99,  80, 15, 'PRD-1002'),
('USB-C Hub 7-in-1',       1, 1, 'Multi-port USB-C hub with HDMI and SD card reader',25.00, 54.99,  60, 10, 'PRD-1003'),
('Monitor 27" 4K',         1, 1, '4K UHD IPS monitor with HDR support',             180.00,349.99,  25,  5, 'PRD-1004'),
('Office Chair',           4, 3, 'Ergonomic office chair with lumbar support',        85.00,199.99,  30, 10, 'PRD-2001'),
('A4 Paper Ream',          4, 3, '500 sheets of 80gsm A4 printing paper',             3.50,  8.99, 500, 50, 'PRD-2002'),
('Ballpoint Pens (Box)',   4, 3, 'Box of 50 blue ballpoint pens',                     4.00,  9.99, 200, 30, 'PRD-2003'),
('Laptop Stand',           4, 3, 'Adjustable aluminum laptop stand',                 18.00, 39.99,  90, 15, 'PRD-2004');

-- Sample customers
INSERT INTO customers (name, phone, email, address) VALUES
('Walk-in Customer', '',              '',                   ''),
('Alice Johnson',    '555-4001', 'alice@example.com',  '10 Main St, Springfield'),
('Bob Martinez',     '555-4002', 'bob@example.com',    '22 Oak Ave, Shelbyville'),
('Carol White',      '555-4003', 'carol@example.com',  '5 Elm St, Capital City');
