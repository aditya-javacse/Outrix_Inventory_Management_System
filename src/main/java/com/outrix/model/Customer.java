package com.outrix.model;

import java.sql.Timestamp;

/** Entity representing a customer. */
public class Customer {
    private int       id;
    private String    name;
    private String    phone;
    private String    email;
    private String    address;
    private Timestamp createdAt;

    public Customer() {}
    public Customer(int id, String name) { this.id = id; this.name = name; }

    public int       getId()                { return id; }
    public void      setId(int id)          { this.id = id; }
    public String    getName()              { return name; }
    public void      setName(String v)      { this.name = v; }
    public String    getPhone()             { return phone; }
    public void      setPhone(String v)     { this.phone = v; }
    public String    getEmail()             { return email; }
    public void      setEmail(String v)     { this.email = v; }
    public String    getAddress()           { return address; }
    public void      setAddress(String v)   { this.address = v; }
    public Timestamp getCreatedAt()         { return createdAt; }
    public void      setCreatedAt(Timestamp v) { this.createdAt = v; }

    @Override public String toString() { return name; }
}
