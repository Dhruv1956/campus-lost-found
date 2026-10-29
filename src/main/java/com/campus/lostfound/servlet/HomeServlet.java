package com.campus.lostfound.servlet;

import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * CONTROLLER for the home page.
 * Pattern used by every servlet in this project (MVC):
 *   1. read the request   2. ask the DAO (model) for data
 *   3. put the data in request attributes   4. forward to a JSP (view) that displays it
 */
@WebServlet(urlPatterns = {"", "/home"})
public class HomeServlet extends HttpServlet {

    private final ItemDAO itemDao = new ItemDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // The site always opens at the login page: visitors who are not logged in
        // are sent there. (Logged-in users see the home page.)
        if (WebUtil.currentUser(req) == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        try {
            req.setAttribute("recent", itemDao.search(null, null, null, "OPEN", 6));
            req.setAttribute("lostCount", itemDao.count("LOST", "OPEN"));
            req.setAttribute("foundCount", itemDao.count("FOUND", "OPEN"));
            req.setAttribute("resolvedCount", itemDao.count(null, "RESOLVED"));
        } catch (SQLException e) {
            throw new ServletException("Could not load home page data", e);
        }
        WebUtil.forward(req, resp, "home.jsp");
    }
}
