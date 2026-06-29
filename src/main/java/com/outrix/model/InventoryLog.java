package com.outrix.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** Entity representing an inventory stock movement record. */
public class InventoryLog {
    private int       id;
    private int       productId;
    private String    productName;     // joined
    private int       userId;
    private String    username;        // joined
    private String    movementType;    // STOCK_IN | STOCK_OUT | ADJUSTMENT | TRANSFER
    private int       quantity;
    private int       previousQty;
    private int       newQty;
    private String    reference;
    private String    notes;
    private Timestamp createdAt;

    public InventoryLog() {}

    public int       getId()                    { return id; }
    public void      setId(int id)              { this.id = id; }
    public int       getProductId()             { return productId; }
    public void      setProductId(int v)        { this.productId = v; }
    public String    getProductName()           { return productName; }
    public void      setProductName(String v)   { this.productName = v; }
    public int       getUserId()                { return userId; }
    public void      setUserId(int v)           { this.userId = v; }
    public String    getUsername()              { return username; }
    public void      setUsername(String v)      { this.username = v; }
    public String    getMovementType()          { return movementType; }
    public void      setMovementType(String v)  { this.movementType = v; }
    public int       getQuantity()              { return quantity; }
    public void      setQuantity(int v)         { this.quantity = v; }
    public int       getPreviousQty()           { return previousQty; }
    public void      setPreviousQty(int v)      { this.previousQty = v; }
    public int       getNewQty()                { return newQty; }
    public void      setNewQty(int v)           { this.newQty = v; }
    public String    getReference()             { return reference; }
    public void      setReference(String v)     { this.reference = v; }
    public String    getNotes()                 { return notes; }
    public void      setNotes(String v)         { this.notes = v; }
    public Timestamp getCreatedAt()             { return createdAt; }
    public void      setCreatedAt(Timestamp v)  { this.createdAt = v; }
}
