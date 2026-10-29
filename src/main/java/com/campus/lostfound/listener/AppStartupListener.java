package com.campus.lostfound.listener;

import com.campus.lostfound.dao.ClaimDAO;
import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.dao.UserDAO;
import com.campus.lostfound.model.Claim;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.User;
import com.campus.lostfound.util.DBUtil;
import com.campus.lostfound.util.PasswordUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LISTENER - runs ONCE when the web application starts (and once when it stops).
 * Jobs at startup:
 *   1. create the database tables (schema.sql)
 *   2. insert demo data if the database is empty (handy for the demonstration)
 *   3. put shared values into the ServletContext ("application scope") for all JSPs
 */
@WebListener
public class AppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        try {
            createTables();
            insertDemoDataIfEmpty();
        } catch (Exception e) {
            // Stop the application from starting with a broken database
            throw new IllegalStateException("Database setup failed: " + e.getMessage(), e);
        }

        // Application-scope attributes: visible in every JSP as ${ctx}, ${categories}, ...
        ctx.setAttribute("ctx", ctx.getContextPath());
        ctx.setAttribute("categories", Item.CATEGORIES);
        ctx.setAttribute("locations", Item.LOCATIONS);
        ctx.setAttribute("activeSessions", new AtomicInteger(0));   // updated by SessionCounterListener

        ctx.log("Campus Lost & Found started. Database ready.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().log("Campus Lost & Found stopped.");
    }

    /** Reads schema.sql from the classpath and runs each statement. */
    private void createTables() throws IOException, SQLException {
        String script;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("schema.sql")) {
            script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        // remove "-- comment" lines, then split into statements at each ';'
        StringBuilder cleaned = new StringBuilder();
        for (String line : script.split("\n")) {
            if (!line.trim().startsWith("--")) {
                cleaned.append(line).append('\n');
            }
        }
        try (Connection con = DBUtil.getConnection(); Statement st = con.createStatement()) {
            for (String sql : cleaned.toString().split(";")) {
                if (!sql.trim().isEmpty()) {
                    st.execute(sql);
                }
            }
        }
    }

    private void insertDemoDataIfEmpty() throws SQLException {
        UserDAO userDao = new UserDAO();
        if (userDao.count() > 0) {
            return;
        }
        int demoId = addUser(userDao, "Demo Student", "demo@campus.edu", "9876543210");
        int priyaId = addUser(userDao, "Priya Nair", "priya@campus.edu", "9123456780");

        ItemDAO itemDao = new ItemDAO();
        itemDao.insert(item(demoId, "LOST", "Black Casio calculator", "fx-991EX with my initials 'DS' on the back cover.",
                "Electronics", "Library", 2));
        itemDao.insert(item(priyaId, "FOUND", "Blue water bottle", "Milton steel bottle found on a bench near the canteen.",
                "Water Bottles", "Main Canteen", 1));
        int idCard = itemDao.insert(item(priyaId, "FOUND", "Student ID card", "ID card found in the corridor. Name starts with 'R'.",
                "ID Cards", "Academic Block", 3));
        itemDao.insert(item(demoId, "LOST", "Set of 3 keys on a red keychain", "Lost somewhere between the hostel and the bus stop.",
                "Keys", "Hostel", 4));
        itemDao.insert(item(priyaId, "LOST", "Data Structures notebook", "Green spiral notebook with a periodic-table sticker.",
                "Books & Notes", "Computer Lab", 5));
        itemDao.insert(item(demoId, "FOUND", "Black wallet", "Leather wallet found near the parking area. Contains cards, no cash.",
                "Wallets & Bags", "Parking Area", 1));

        // one pending claim, so the claim workflow can be shown immediately
        Claim claim = new Claim();
        claim.setItemId(idCard);
        claim.setClaimerId(demoId);
        claim.setMessage("This looks like my friend's ID card. The name starts with R and the department is CSE.");
        new ClaimDAO().insert(claim);
    }

    private int addUser(UserDAO dao, String name, String email, String phone) throws SQLException {
        User u = new User();
        u.setFullName(name);
        u.setEmail(email);
        u.setPhone(phone);
        u.setSalt(PasswordUtil.newSalt());
        u.setPasswordHash(PasswordUtil.hash("demo123", u.getSalt()));   // demo password for everyone
        return dao.create(u);
    }

    private Item item(int userId, String type, String title, String desc, String category, String location, int daysAgo) {
        Item it = new Item();
        it.setUserId(userId);
        it.setType(type);
        it.setTitle(title);
        it.setDescription(desc);
        it.setCategory(category);
        it.setLocation(location);
        it.setEventDate(Date.valueOf(LocalDate.now().minusDays(daysAgo)));
        return it;
    }
}
