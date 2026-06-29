package com.outrix.model;

import java.sql.Timestamp;

/** Entity representing an audit activity log entry. */
public class ActivityLog {
    private int       id;
    private int       userId;
    private String    username;
    private String    action;
    private String    description;
    private String    ipAddress;
    private Timestamp createdAt;

    public ActivityLog() {}

    public int       getId()                  { return id; }
    public void      setId(int id)            { this.id = id; }
    public int       getUserId()              { return userId; }
    public void      setUserId(int v)         { this.userId = v; }
    public String    getUsername()            { return username; }
    public void      setUsername(String v)    { this.username = v; }
    public String    getAction()              { return action; }
    public void      setAction(String v)      { this.action = v; }
    public String    getDescription()         { return description; }
    public void      setDescription(String v) { this.description = v; }
    public String    getIpAddress()           { return ipAddress; }
    public void      setIpAddress(String v)   { this.ipAddress = v; }
    public Timestamp getCreatedAt()           { return createdAt; }
    public void      setCreatedAt(Timestamp v){ this.createdAt = v; }
}
