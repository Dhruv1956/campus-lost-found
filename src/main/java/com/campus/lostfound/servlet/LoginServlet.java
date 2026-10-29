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
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/** Login: checks e-mail + password and stores the User in the HTTP session. */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDao = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (WebUtil.currentUser(req) != null) {                      // already logged in
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }
        WebUtil.forward(req, resp, "login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = WebUtil.param(req, "email").toLowerCase();
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");

        User user;
        try {
            user = userDao.findByEmail(email);
        } catch (SQLException e) {
            throw new ServletException("Login failed", e);
        }

        if (user == null || !PasswordUtil.verify(password, user.getSalt(), user.getPasswordHash())) {
            req.setAttribute("error", "Invalid e-mail or password.");   // same message for both cases
            WebUtil.forward(req, resp, "login.jsp");
            return;
        }

        // Login OK. Do not keep the password hash in the session.
        user.setSalt(null);
        user.setPasswordHash(null);

        HttpSession session = req.getSession();
        String afterLogin = (String) session.getAttribute("afterLogin");
        req.changeSessionId();                       // new session id after login (prevents session fixation)
        session.setAttribute("user", user);
        session.removeAttribute("afterLogin");
        session.setMaxInactiveInterval(30 * 60);     // log out automatically after 30 minutes of inactivity

        WebUtil.flash(req, "success", "Welcome, " + user.getFullName() + "!");

        // go back to the page the user wanted - but only to a path inside this application
        String target = "/";
        if (afterLogin != null && afterLogin.startsWith("/") && !afterLogin.startsWith("//")) {
            target = afterLogin;
        }
        resp.sendRedirect(req.getContextPath() + target);
    }
}
