# 📦 Outrix ERP Inventory Management System

🚀 **Outrix ERP** is a modern, professional, Java-based Enterprise Resource Planning (ERP) and Inventory Management System. It features a sleek desktop UI using FlatLaf and provides secure authentication, role-based access control, real-time stock tracking, sales analytics, reporting, and automated database backups.

---

## ✨ Features

- **🔐 Secure Authentication & RBAC**: Dual-role login structure (`ADMIN` and `EMPLOYEE`) secured by BCrypt password hashing.
- **📦 Inventory & Product Tracking**: Manage product catalogs, automatic low-stock alerts, and category classifications.
- **🏷 Barcode & QR Code Integration**: Generate and scan product codes (using ZXing).
- **🚚 Supplier Management**: Track bulk supply sources and purchase histories.
- **💰 Sales & Invoicing**: Interactive checkout process with automatic stock level adjustments, calculations (discount, tax, subtotal), and custom receipts.
- **📊 Analytics & Dashboards**: Visual charts for revenue, popular categories, and metrics (using JFreeChart).
- **📄 Professional Reports**: Generate PDF reports (via iText 7) and Excel exports (via Apache POI) for products, inventory, and sales.
- **📋 Audit Trails**: System activity logging to record crucial actions.
- **💾 Automatic Backups**: Direct backup and restore capability.

---

## 🛠 Tech Stack

- **Core Language**: Java (JDK 17 or higher recommended)
- **GUI Framework**: Java Swing with **FlatLaf** (FlatDarkLaf theme)
- **Database Engine**: MySQL 8.x
- **ORM & Drivers**: JDBC (MySQL Connector/J)
- **Reporting & Exports**: 
  - **iText 7 Core** (PDF Generation)
  - **Apache POI** (Excel Sheet Exports)
- **Analytics**: **JFreeChart**
- **Security**: **jBCrypt** (Password hashing)
- **Barcodes**: **ZXing (Zebra Crossing)**

---

## 🚀 Setup & Installation

### 1. Prerequisites
- **Java SE Development Kit (JDK) 17+**
- **MySQL Server 8.x+**
- **Maven** (integrated or standalone)

### 2. Database Initialization
1. Start your local MySQL server.
2. Create the database and seed the tables by executing the [schema.sql](database/schema.sql) script:
   ```sql
   source database/schema.sql;
   ```

### 3. Connection Configuration
Ensure the JDBC connection details match your database settings in the configuration file:
- **Location**: [DBConnection.java](src/main/java/com/outrix/config/DBConnection.java)
- **Properties**:
  - `HOST`: `localhost`
  - `PORT`: `3306`
  - `DATABASE`: `outrix_erp`
  - `USER`: `root`
  - `PASSWORD`: `04062005aditya`

---

## 🖥 How to Run

### Option A: Double-Click Launcher (Windows)
Double-click the pre-configured [run.bat](run.bat) file in the project root. It will automatically build the source code (if target binaries are missing) and launch the application desktop window.

### Option B: Terminal Command (Maven)
Build the JAR and execute:
```bash
mvn clean package
java -jar target/outrix-erp-1.0.0.jar
```

---

## 🔑 Default Login Credentials

Use the following default accounts to log in upon launching the application:

| Role | Username | Password |
| :--- | :--- | :--- |
| **System Administrator** | `admin` | `Admin@123` |
| **Sales Associate** | `employee1` | `Admin@123` |

---

## 📂 Project Structure

```
Outrix_Inventory_Management_System/
│
├── database/
│   └── schema.sql          # MySQL database schema & seed data
│
├── src/main/java/com/outrix/
│   ├── Main.java           # Main entry point
│   ├── config/             # DB connection managers
│   ├── dao/                # Data Access Objects (SQL query mapping)
│   ├── model/              # Domain entities (User, Product, Category, etc.)
│   ├── util/               # PDF, Excel, password, and barcode utilities
│   └── view/               # Swing GUI panels and components
│
├── pom.xml                 # Maven dependencies & shaded build plugins
└── run.bat                 # Convenient launch script for Windows
```
