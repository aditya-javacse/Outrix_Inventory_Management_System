package com.outrix.model;

import java.sql.Timestamp;

/**
 * Entity representing a system user (login account).
 */
public class User {

    private int       id;
    private String    username;
    private String    password;
    private String    role;       // "ADMIN" | "EMPLOYEE"
    private String    email;
    private String    fullName;
    private boolean   isActive;
    private Timestamp lastLogin;
    private Timestamp createdAt;

    public User() {}

    public User(int id, String username, String password, String role,
                String email, String fullName, boolean isActive,
                Timestamp lastLogin, Timestamp createdAt) {
        this.id        = id;
        this.username  = username;
        this.password  = password;
        this.role      = role;
        this.email     = email;
        this.fullName  = fullName;
        this.isActive  = isActive;
        this.lastLogin = lastLogin;
        this.createdAt = createdAt;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int       getId()        { return id; }
    public void      setId(int id)  { this.id = id; }

    public String    getUsername()              { return username; }
    public void      setUsername(String v)      { this.username = v; }

    public String    getPassword()              { return password; }
    public void      setPassword(String v)      { this.password = v; }

    public String    getRole()                  { return role; }
    public void      setRole(String v)          { this.role = v; }

    public String    getEmail()                 { return email; }
    public void      setEmail(String v)         { this.email = v; }

    public String    getFullName()              { return fullName; }
    public void      setFullName(String v)      { this.fullName = v; }

    public boolean   isActive()                 { return isActive; }
    public void      setActive(boolean v)       { this.isActive = v; }

    public Timestamp getLastLogin()             { return lastLogin; }
    public void      setLastLogin(Timestamp v)  { this.lastLogin = v; }

    public Timestamp getCreatedAt()             { return createdAt; }
    public void      setCreatedAt(Timestamp v)  { this.createdAt = v; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role='" + role + "'}";
    }
}
