package com.outrix.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Entity representing an employee record.
 */
public class Employee {

    private int       id;
    private int       userId;
    private String    name;
    private String    email;
    private String    phone;
    private String    role;
    private Date      hireDate;
    private Timestamp createdAt;

    // Username/password for display purposes (joined from users table)
    private String    username;
    private String    userRole; // ADMIN | EMPLOYEE

    public Employee() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int       getId()                    { return id; }
    public void      setId(int id)              { this.id = id; }

    public int       getUserId()                { return userId; }
    public void      setUserId(int v)           { this.userId = v; }

    public String    getName()                  { return name; }
    public void      setName(String v)          { this.name = v; }

    public String    getEmail()                 { return email; }
    public void      setEmail(String v)         { this.email = v; }

    public String    getPhone()                 { return phone; }
    public void      setPhone(String v)         { this.phone = v; }

    public String    getRole()                  { return role; }
    public void      setRole(String v)          { this.role = v; }

    public Date      getHireDate()              { return hireDate; }
    public void      setHireDate(Date v)        { this.hireDate = v; }

    public Timestamp getCreatedAt()             { return createdAt; }
    public void      setCreatedAt(Timestamp v)  { this.createdAt = v; }

    public String    getUsername()              { return username; }
    public void      setUsername(String v)      { this.username = v; }

    public String    getUserRole()              { return userRole; }
    public void      setUserRole(String v)      { this.userRole = v; }

    @Override
    public String toString() { return name; }
}
