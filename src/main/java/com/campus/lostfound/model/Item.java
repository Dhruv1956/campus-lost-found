package com.campus.lostfound.model;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

/**
 * Model (JavaBean) for one row of the "items" table (a LOST or FOUND report).
 * The last four fields (owner... and pendingClaims) are not columns of "items";
 * they are filled by the JOIN / sub-query in ItemDAO so the JSP can show them.
 */
public class Item implements Serializable {
    private static final long serialVersionUID = 1L;

    /** The categories offered in the drop-downs. */
    public static final List<String> CATEGORIES = Arrays.asList(
            "Electronics", "ID Cards", "Books & Notes", "Keys", "Wallets & Bags",
            "Clothing", "Water Bottles", "Stationery", "Others");

    /** Common campus places, used as suggestions in the "location" box. */
    public static final List<String> LOCATIONS = Arrays.asList(
            "Library", "Main Canteen", "Academic Block", "Hostel", "Sports Complex",
            "Auditorium", "Parking Area", "Bus Stop", "Computer Lab");

    private int id;
    private int userId;
    private String type;          // "LOST" or "FOUND"
    private String title;
    private String description;
    private String category;
    private String location;
    private Date eventDate;       // the day it was lost / found
    private String status;        // "OPEN" or "RESOLVED"
    private Timestamp createdAt;

    // extra, filled by joins
    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;
    private int pendingClaims;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Date getEventDate() { return eventDate; }
    public void setEventDate(Date eventDate) { this.eventDate = eventDate; }


    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }

    public int getPendingClaims() { return pendingClaims; }
    public void setPendingClaims(int pendingClaims) { this.pendingClaims = pendingClaims; }
}
