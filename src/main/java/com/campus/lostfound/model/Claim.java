package com.campus.lostfound.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Model (JavaBean) for one row of the "claims" table.
 * A claim is a message from one user to the owner of an item:
 *   - on a FOUND item:  "I think this is mine, here is the proof..."
 *   - on a LOST item:   "I found this, here is where I kept it..."
 * The owner then APPROVES or REJECTS it.
 */
public class Claim implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int itemId;
    private int claimerId;
    private String message;
    private String status;        // PENDING, APPROVED or REJECTED
    private Timestamp createdAt;

    // extra, filled by joins
    private String claimerName;
    private String claimerEmail;
    private String claimerPhone;
    private String itemTitle;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getClaimerId() { return claimerId; }
    public void setClaimerId(int claimerId) { this.claimerId = claimerId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getClaimerName() { return claimerName; }
    public void setClaimerName(String claimerName) { this.claimerName = claimerName; }

    public String getClaimerEmail() { return claimerEmail; }
    public void setClaimerEmail(String claimerEmail) { this.claimerEmail = claimerEmail; }

    public String getClaimerPhone() { return claimerPhone; }
    public void setClaimerPhone(String claimerPhone) { this.claimerPhone = claimerPhone; }

    public String getItemTitle() { return itemTitle; }
    public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }
}
