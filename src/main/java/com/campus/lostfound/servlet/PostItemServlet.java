package com.campus.lostfound.servlet;

import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.User;
import com.campus.lostfound.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * "Post an item" page. Needs login (see AuthFilter).
 */
@WebServlet("/post")
public class PostItemServlet extends HttpServlet {

    private final ItemDAO itemDao = new ItemDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        showForm(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<String> errors = new ArrayList<>();

        String type = WebUtil.param(req, "type").toUpperCase();
        String title = WebUtil.param(req, "title");
        String description = WebUtil.param(req, "description");
        String category = WebUtil.param(req, "category");
        String location = WebUtil.param(req, "location");
        String dateText = WebUtil.param(req, "eventDate");

        // ---- server-side validation ----
        if (!type.equals("LOST") && !type.equals("FOUND")) {
            errors.add("Please choose whether the item is Lost or Found.");
        }
        if (title.length() < 3 || title.length() > 120) {
            errors.add("Title must be 3 to 120 characters.");
        }
        if (description.length() > 1000) {
            errors.add("Description can be at most 1000 characters.");
        }
        if (!Item.CATEGORIES.contains(category)) {
            errors.add("Please choose a category.");
        }
        if (location.isEmpty() || location.length() > 120) {
            errors.add("Location is required (maximum 120 characters).");
        }
        Date eventDate = null;
        try {
            LocalDate d = LocalDate.parse(dateText);
            if (d.isAfter(LocalDate.now())) {
                errors.add("The date cannot be in the future.");
            } else {
                eventDate = Date.valueOf(d);
            }
        } catch (DateTimeParseException e) {
            errors.add("Please enter a valid date.");
        }

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            showForm(req, resp);
            return;
        }

        User user = WebUtil.currentUser(req);
        Item item = new Item();
        item.setUserId(user.getId());
        item.setType(type);
        item.setTitle(title);
        item.setDescription(description.isEmpty() ? null : description);
        item.setCategory(category);
        item.setLocation(location);
        item.setEventDate(eventDate);

        int newId;
        try {
            newId = itemDao.insert(item);
        } catch (SQLException e) {
            throw new ServletException("Could not save item", e);
        }

        WebUtil.flash(req, "success", "Your " + type.toLowerCase() + " report has been posted.");
        resp.sendRedirect(req.getContextPath() + "/item?id=" + newId);
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("today", LocalDate.now().toString());     // used as the max value of the date picker
        WebUtil.forward(req, resp, "post-item.jsp");
    }
}
