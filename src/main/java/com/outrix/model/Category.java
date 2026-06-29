package com.outrix.model;

import java.sql.Timestamp;

/** Entity representing a product category. */
public class Category {
    private int       id;
    private String    name;
    private String    description;
    private Timestamp createdAt;

    public Category() {}
    public Category(int id, String name) { this.id = id; this.name = name; }

    public int       getId()               { return id; }
    public void      setId(int id)         { this.id = id; }
    public String    getName()             { return name; }
    public void      setName(String v)     { this.name = v; }
    public String    getDescription()      { return description; }
    public void      setDescription(String v) { this.description = v; }
    public Timestamp getCreatedAt()        { return createdAt; }
    public void      setCreatedAt(Timestamp v) { this.createdAt = v; }

    @Override public String toString() { return name; }
}
