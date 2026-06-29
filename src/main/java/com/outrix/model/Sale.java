package com.outrix.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/** Entity representing a sales invoice (header). */
public class Sale {
    private int           id;
    private String        invoiceNumber;
    private Integer       customerId;
    private String        customerName;   // joined
    private Integer       userId;
    private String        username;       // joined
    private BigDecimal    totalAmount;
    private BigDecimal    discount;
    private BigDecimal    tax;
    private BigDecimal    grandTotal;
    private String        paymentMethod;
    private String        notes;
    private Timestamp     saleDate;
    private List<SaleItem> items;         // line items (not always loaded)

    public Sale() {}

    public int          getId()                      { return id; }
    public void         setId(int id)                { this.id = id; }
    public String       getInvoiceNumber()           { return invoiceNumber; }
    public void         setInvoiceNumber(String v)   { this.invoiceNumber = v; }
    public Integer      getCustomerId()              { return customerId; }
    public void         setCustomerId(Integer v)     { this.customerId = v; }
    public String       getCustomerName()            { return customerName; }
    public void         setCustomerName(String v)    { this.customerName = v; }
    public Integer      getUserId()                  { return userId; }
    public void         setUserId(Integer v)         { this.userId = v; }
    public String       getUsername()                { return username; }
    public void         setUsername(String v)        { this.username = v; }
    public BigDecimal   getTotalAmount()             { return totalAmount; }
    public void         setTotalAmount(BigDecimal v) { this.totalAmount = v; }
    public BigDecimal   getDiscount()                { return discount; }
    public void         setDiscount(BigDecimal v)    { this.discount = v; }
    public BigDecimal   getTax()                     { return tax; }
    public void         setTax(BigDecimal v)         { this.tax = v; }
    public BigDecimal   getGrandTotal()              { return grandTotal; }
    public void         setGrandTotal(BigDecimal v)  { this.grandTotal = v; }
    public String       getPaymentMethod()           { return paymentMethod; }
    public void         setPaymentMethod(String v)   { this.paymentMethod = v; }
    public String       getNotes()                   { return notes; }
    public void         setNotes(String v)           { this.notes = v; }
    public Timestamp    getSaleDate()                { return saleDate; }
    public void         setSaleDate(Timestamp v)     { this.saleDate = v; }
    public List<SaleItem> getItems()                 { return items; }
    public void         setItems(List<SaleItem> v)   { this.items = v; }
}
