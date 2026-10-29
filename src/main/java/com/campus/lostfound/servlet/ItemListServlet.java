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

/** Browse / search page: /items?q=wallet&type=FOUND&category=Keys&status=OPEN */
@WebServlet("/items")
public class ItemListServlet extends HttpServlet {

    private final ItemDAO itemDao = new ItemDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String q = WebUtil.param(req, "q");
        String type = WebUtil.param(req, "type");
        String category = WebUtil.param(req, "category");

        // status filter: default is OPEN; the value "ALL" means "no filter"
        String status = WebUtil.param(req, "status");
        if (status.isEmpty()) {
            status = "OPEN";
        }
        if (status.equals("ALL")) {
            status = null;
        }

        try {
            req.setAttribute("items", itemDao.search(type, category, q, status, 0));
        } catch (SQLException e) {
            throw new ServletException("Could not search items", e);
        }
        WebUtil.forward(req, resp, "items.jsp");
    }
}
