package com.campus.lostfound.servlet;

import com.campus.lostfound.dao.ClaimDAO;
import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.model.User;
import com.campus.lostfound.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/** Dashboard of the logged-in user: items he/she posted + claims he/she sent. */
@WebServlet("/my-items")
public class MyItemsServlet extends HttpServlet {

    private final ItemDAO itemDao = new ItemDAO();
    private final ClaimDAO claimDao = new ClaimDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = WebUtil.currentUser(req);
        try {
            req.setAttribute("myItems", itemDao.findByUser(user.getId()));
            req.setAttribute("myClaims", claimDao.findByClaimer(user.getId()));
        } catch (SQLException e) {
            throw new ServletException("Could not load your items", e);
        }
        WebUtil.forward(req, resp, "my-items.jsp");
    }
}
