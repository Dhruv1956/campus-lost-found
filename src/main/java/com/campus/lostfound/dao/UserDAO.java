package com.campus.lostfound.dao;

import com.campus.lostfound.model.User;
import com.campus.lostfound.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DAO = Data Access Object. All SQL for the "users" table lives here,
 * so servlets never contain SQL and JSPs never touch the database.
 * We use PreparedStatement with ? placeholders -> protects against SQL injection.
 */
public class UserDAO {

    /** Inserts a new user and returns the generated id. */
    public int create(User u) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, phone, salt, password_hash) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPhone());
            ps.setString(4, u.getSalt());
            ps.setString(5, u.getPasswordHash());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    /** Returns the user with this email, or null if there is none. */
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public int count() throws SQLException {
        try (Connection con = DBUtil.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** Converts the current row of a ResultSet into a User object. */
    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setSalt(rs.getString("salt"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}
