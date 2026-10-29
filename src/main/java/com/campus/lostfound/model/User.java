package com.campus.lostfound.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Model (JavaBean) for one row of the "users" table.
 * Serializable because the logged-in User object is stored in the HttpSession.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String fullName;
    private String email;
    private String phone;
    private String salt;
    private String passwordHash;
    private Timestamp createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
