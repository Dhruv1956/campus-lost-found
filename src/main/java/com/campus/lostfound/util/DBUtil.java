package com.campus.lostfound.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central place for JDBC connection settings.
 * Reads db.properties once (static block), loads the JDBC driver,
 * and hands out a fresh Connection with getConnection().
 *
 * Every DAO method opens its own connection in a try-with-resources block,
 * so the connection is always closed - even when an exception happens.
 * (A bigger application would use a connection pool instead of DriverManager.)
 */
public final class DBUtil {

    private static final String DB_URL;
    private static final String DB_USER;
    private static final String DB_PASSWORD;

    static {
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties p = new Properties();
            p.load(in);
            Class.forName(p.getProperty("db.driver"));              // step 1 of JDBC: load the driver
            String home = System.getProperty("user.home").replace('\\', '/');
            DB_URL = p.getProperty("db.url").replace("${user.home}", home);
            DB_USER = p.getProperty("db.user");
            DB_PASSWORD = p.getProperty("db.password", "");
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBUtil() { }

    /** Step 2 of JDBC: open a connection to the database. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
