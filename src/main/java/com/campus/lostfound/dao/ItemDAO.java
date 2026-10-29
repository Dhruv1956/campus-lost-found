package com.campus.lostfound.dao;

import com.campus.lostfound.model.Item;
import com.campus.lostfound.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** DAO for the "items" table (lost / found reports). */
public class ItemDAO {

    /**
     * Base query used by every read method.
     *  - JOIN users        -> gets the name/email/phone of the person who posted the item
     *  - sub-query COUNT   -> number of claims still waiting for the owner's decision
     */
    private static final String BASE_SELECT =
            "SELECT i.*, u.full_name AS owner_name, u.email AS owner_email, u.phone AS owner_phone, "
          + "(SELECT COUNT(*) FROM claims c WHERE c.item_id = i.id AND c.status = 'PENDING') AS pending_claims "
          + "FROM items i JOIN users u ON u.id = i.user_id ";

    /** Inserts a new item and returns its generated id. */
    public int insert(Item item) throws SQLException {
        String sql = "INSERT INTO items (user_id, type, title, description, category, location, event_date) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getUserId());
            ps.setString(2, item.getType());
            ps.setString(3, item.getTitle());
            ps.setString(4, item.getDescription());
            ps.setString(5, item.getCategory());
            ps.setString(6, item.getLocation());
            ps.setDate(7, item.getEventDate());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    /** Returns one item (with owner details), or null if the id does not exist. */
    public Item findById(int id) throws SQLException {
        String sql = BASE_SELECT + "WHERE i.id = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Search / filter used by the Browse page and the home page.
     * Any argument that is null or empty is simply ignored.
     * The SQL text is built step by step, but the VALUES always go through "?" placeholders.
     *
     * @param type     "LOST", "FOUND" or null for both
     * @param category one of Item.CATEGORIES or null for all
     * @param keyword  searched (case-insensitive) in title, description and location
     * @param status   "OPEN", "RESOLVED" or null for both
     * @param limit    maximum rows, 0 = no limit
     */
    public List<Item> search(String type, String category, String keyword, String status, int limit)
            throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1 = 1");
        List<Object> params = new ArrayList<>();

        if (notBlank(type)) {
            sql.append(" AND i.type = ?");
            params.add(type);
        }
        if (notBlank(category)) {
            sql.append(" AND i.category = ?");
            params.add(category);
        }
        if (notBlank(status)) {
            sql.append(" AND i.status = ?");
            params.add(status);
        }
        if (notBlank(keyword)) {
            sql.append(" AND (LOWER(i.title) LIKE ? OR LOWER(i.description) LIKE ? OR LOWER(i.location) LIKE ?)");
            String like = "%" + keyword.toLowerCase() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        sql.append(" ORDER BY i.created_at DESC, i.id DESC");
        if (limit > 0) {
            sql.append(" LIMIT ").append(limit);
        }

        List<Item> result = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }
        return result;
    }

    /** All items posted by one user (for the "My Items" page). */
    public List<Item> findByUser(int userId) throws SQLException {
        String sql = BASE_SELECT + "WHERE i.user_id = ? ORDER BY i.created_at DESC, i.id DESC";
        List<Item> result = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }
        return result;
    }

    /** Counts items for the statistics on the home page. type or status may be null. */
    public int count(String type, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM items WHERE 1 = 1");
        List<String> params = new ArrayList<>();
        if (notBlank(type)) {
            sql.append(" AND type = ?");
            params.add(type);
        }
        if (notBlank(status)) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void markResolved(int itemId) throws SQLException {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE items SET status = 'RESOLVED' WHERE id = ?")) {
            ps.setInt(1, itemId);
            ps.executeUpdate();
        }
    }

    /**
     * Deletes an item together with its claims.
     * This is a TRANSACTION: either both deletes happen, or (on error) neither does.
     * (Claims must go first because of the foreign key claims.item_id -> items.id.)
     */
    public void delete(int itemId) throws SQLException {
        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);                       // start of the transaction
            try (PreparedStatement delClaims = con.prepareStatement("DELETE FROM claims WHERE item_id = ?");
                 PreparedStatement delItem = con.prepareStatement("DELETE FROM items WHERE id = ?")) {
                delClaims.setInt(1, itemId);
                delClaims.executeUpdate();
                delItem.setInt(1, itemId);
                delItem.executeUpdate();
                con.commit();                               // make both changes permanent
            } catch (SQLException e) {
                con.rollback();                             // undo everything
                throw e;
            }
        }
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    /** Converts the current row of a ResultSet into an Item object. */
    private Item map(ResultSet rs) throws SQLException {
        Item it = new Item();
        it.setId(rs.getInt("id"));
        it.setUserId(rs.getInt("user_id"));
        it.setType(rs.getString("type"));
        it.setTitle(rs.getString("title"));
        it.setDescription(rs.getString("description"));
        it.setCategory(rs.getString("category"));
        it.setLocation(rs.getString("location"));
        it.setEventDate(rs.getDate("event_date"));
        it.setStatus(rs.getString("status"));
        it.setCreatedAt(rs.getTimestamp("created_at"));
        it.setOwnerName(rs.getString("owner_name"));
        it.setOwnerEmail(rs.getString("owner_email"));
        it.setOwnerPhone(rs.getString("owner_phone"));
        it.setPendingClaims(rs.getInt("pending_claims"));
        return it;
    }
}
