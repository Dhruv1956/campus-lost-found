package com.campus.lostfound.servlet;

import com.campus.lostfound.dao.UserDAO;
import com.campus.lostfound.model.User;
import com.campus.lostfound.util.PasswordUtil;
import com.campus.lostfound.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Sign-up: GET shows the form, POST validates it and creates the user. */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDao = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        WebUtil.forward(req, resp, "register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = WebUtil.param(req, "fullName");
        String email = WebUtil.param(req, "email").toLowerCase();
        String phone = WebUtil.param(req, "phone");
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        String confirm = req.getParameter("confirm") == null ? "" : req.getParameter("confirm");

        // ---- server-side validation (never trust the browser alone) ----
        List<String> errors = new ArrayList<>();
        if (name.length() < 2 || name.length() > 100) {
            errors.add("Name must be 2 to 100 characters.");
        }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") || email.length() > 120) {
            errors.add("Enter a valid e-mail address.");
        }
        if (!phone.isEmpty() && !phone.matches("^[0-9+\\- ]{7,15}$")) {
            errors.add("Phone number may contain only digits, +, - and spaces (7 to 15 characters).");
        }
        if (password.length() < 6) {
            errors.add("Password must be at least 6 characters.");
        }
        if (!password.equals(confirm)) {
            errors.add("Passwords do not match.");
        }

        try {
            if (errors.isEmpty() && userDao.findByEmail(email) != null) {
                errors.add("An account with this e-mail already exists.");
            }
            if (!errors.isEmpty()) {
                req.setAttribute("errors", errors);
                WebUtil.forward(req, resp, "register.jsp");
                return;
            }

            User u = new User();
            u.setFullName(name);
            u.setEmail(email);
            u.setPhone(phone.isEmpty() ? null : phone);
            u.setSalt(PasswordUtil.newSalt());
            u.setPasswordHash(PasswordUtil.hash(password, u.getSalt()));
            userDao.create(u);
        } catch (SQLException e) {
            throw new ServletException("Could not create account", e);
        }

        WebUtil.flash(req, "success", "Account created. Please log in.");
        resp.sendRedirect(req.getContextPath() + "/login");       // Post-Redirect-Get pattern
    }
}
