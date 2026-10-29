package com.campus.lostfound.dao;

import com.campus.lostfound.model.Claim;
import com.campus.lostfound.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO for the "claims" table. */
public class ClaimDAO {

    /** JOINs bring in the claimer's contact details and the title of the item. */
    private static final String BASE_SELECT =
            "SELECT c.*, u.full_name AS claimer_name, u.email AS claimer_email, u.phone AS claimer_phone, "
          + "i.title AS item_title "
          + "FROM claims c JOIN users u ON u.id = c.claimer_id JOIN items i ON i.id = c.item_id ";

    public int insert(Claim c) throws SQLException {
        String sql = "INSERT INTO claims (item_id, claimer_id, message) VALUES (?, ?, ?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getItemId());
            ps.setInt(2, c.getClaimerId());
            ps.setString(3, c.getMessage());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public Claim findById(int claimId) throws SQLException {
        return queryOne(BASE_SELECT + "WHERE c.id = ?", claimId);
    }

    /** The claim a given user already made on a given item (or null). Used to stop duplicates. */
    public Claim findByItemAndClaimer(int itemId, int claimerId) throws SQLException {
        String sql = BASE_SELECT + "WHERE c.item_id = ? AND c.claimer_id = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setInt(2, claimerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** All claims on one item (shown to the item's owner). */
    public List<Claim> findByItem(int itemId) throws SQLException {
        return queryList(BASE_SELECT + "WHERE c.item_id = ? ORDER BY c.created_at DESC, c.id DESC", itemId);
    }

    /** All claims made by one user (shown on "My Items"). */
    public List<Claim> findByClaimer(int claimerId) throws SQLException {
        return queryList(BASE_SELECT + "WHERE c.claimer_id = ? ORDER BY c.created_at DESC, c.id DESC", claimerId);
    }

    /**
     * Owner's decision on a claim - a TRANSACTION with several updates:
     *   approve: this claim -> APPROVED, all other pending claims -> REJECTED, item -> RESOLVED
     *   reject : this claim -> REJECTED
     * If any statement fails, rollback() undoes all of them, so the data is never half-updated.
     */
    public void decide(int claimId, int itemId, boolean approve) throws SQLException {
        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement("UPDATE claims SET status = ? WHERE id = ?")) {
                    ps.setString(1, approve ? "APPROVED" : "REJECTED");
                    ps.setInt(2, claimId);
                    ps.executeUpdate();
                }
                if (approve) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE claims SET status = 'REJECTED' WHERE item_id = ? AND id <> ? AND status = 'PENDING'")) {
                        ps.setInt(1, itemId);
                        ps.setInt(2, claimId);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement("UPDATE items SET status = 'RESOLVED' WHERE id = ?")) {
                        ps.setInt(1, itemId);
                        ps.executeUpdate();
                    }
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // ---------- small private helpers ----------

    private Claim queryOne(String sql, int param) throws SQLException {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private List<Claim> queryList(String sql, int param) throws SQLException {
        List<Claim> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    private Claim map(ResultSet rs) throws SQLException {
        Claim c = new Claim();
        c.setId(rs.getInt("id"));
        c.setItemId(rs.getInt("item_id"));
        c.setClaimerId(rs.getInt("claimer_id"));
        c.setMessage(rs.getString("message"));
        c.setStatus(rs.getString("status"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        c.setClaimerName(rs.getString("claimer_name"));
        c.setClaimerEmail(rs.getString("claimer_email"));
        c.setClaimerPhone(rs.getString("claimer_phone"));
        c.setItemTitle(rs.getString("item_title"));
        return c;
    }
}
