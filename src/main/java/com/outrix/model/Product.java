package com.outrix.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Entity representing a product in the catalog.
 */
public class Product {

    private int        id;
    private String     productName;
    private int        categoryId;
    private String     categoryName;   // joined field
    private Integer    supplierId;
    private String     supplierName;   // joined field
    private String     description;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private int        quantity;
    private int        lowStockThreshold;
    private String     barcode;
    private Timestamp  dateAdded;
    private Timestamp  updatedAt;

    public Product() {}

    // ── Convenience helpers ───────────────────────────────────────────────────

    public boolean isLowStock() {
        return quantity <= lowStockThreshold && quantity > 0;
    }

    public boolean isOutOfStock() {
        return quantity <= 0;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int        getId()                      { return id; }
    public void       setId(int id)                { this.id = id; }

    public String     getProductName()             { return productName; }
    public void       setProductName(String v)     { this.productName = v; }

    public int        getCategoryId()              { return categoryId; }
    public void       setCategoryId(int v)         { this.categoryId = v; }

    public String     getCategoryName()            { return categoryName; }
    public void       setCategoryName(String v)    { this.categoryName = v; }

    public Integer    getSupplierId()              { return supplierId; }
    public void       setSupplierId(Integer v)     { this.supplierId = v; }

    public String     getSupplierName()            { return supplierName; }
    public void       setSupplierName(String v)    { this.supplierName = v; }

    public String     getDescription()             { return description; }
    public void       setDescription(String v)     { this.description = v; }

    public BigDecimal getPurchasePrice()           { return purchasePrice; }
    public void       setPurchasePrice(BigDecimal v) { this.purchasePrice = v; }

    public BigDecimal getSellingPrice()            { return sellingPrice; }
    public void       setSellingPrice(BigDecimal v){ this.sellingPrice = v; }

    public int        getQuantity()                { return quantity; }
    public void       setQuantity(int v)           { this.quantity = v; }

    public int        getLowStockThreshold()       { return lowStockThreshold; }
    public void       setLowStockThreshold(int v)  { this.lowStockThreshold = v; }

    public String     getBarcode()                 { return barcode; }
    public void       setBarcode(String v)         { this.barcode = v; }

    public Timestamp  getDateAdded()               { return dateAdded; }
    public void       setDateAdded(Timestamp v)    { this.dateAdded = v; }

    public Timestamp  getUpdatedAt()               { return updatedAt; }
    public void       setUpdatedAt(Timestamp v)    { this.updatedAt = v; }

    @Override
    public String toString() { return productName; }
}
