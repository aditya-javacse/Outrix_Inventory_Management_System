package com.outrix.model;

import java.math.BigDecimal;

/** Entity representing a line item within a sale invoice. */
public class SaleItem {
    private int        id;
    private int        saleId;
    private int        productId;
    private String     productName;  // joined
    private int        quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public SaleItem() {}

    public SaleItem(int productId, String productName, int quantity, BigDecimal unitPrice) {
        this.productId   = productId;
        this.productName = productName;
        this.quantity    = quantity;
        this.unitPrice   = unitPrice;
        this.subtotal    = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public int        getId()                      { return id; }
    public void       setId(int id)                { this.id = id; }
    public int        getSaleId()                  { return saleId; }
    public void       setSaleId(int v)             { this.saleId = v; }
    public int        getProductId()               { return productId; }
    public void       setProductId(int v)          { this.productId = v; }
    public String     getProductName()             { return productName; }
    public void       setProductName(String v)     { this.productName = v; }
    public int        getQuantity()                { return quantity; }
    public void       setQuantity(int v)           { this.quantity = v; recalc(); }
    public BigDecimal getUnitPrice()               { return unitPrice; }
    public void       setUnitPrice(BigDecimal v)   { this.unitPrice = v; recalc(); }
    public BigDecimal getSubtotal()                { return subtotal; }
    public void       setSubtotal(BigDecimal v)    { this.subtotal = v; }

    private void recalc() {
        if (unitPrice != null && quantity > 0) {
            subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
