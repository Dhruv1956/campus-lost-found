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
import java.sql.SQLException;

/** The owner of an item can mark it resolved or delete it (POST /manage, action=resolve|delete). */
@WebServlet("/manage")
public class ManageItemServlet extends HttpServlet {

    private final ItemDAO itemDao = new ItemDAO();

    /** Typing /manage in the address bar (GET) is not an action - just go back to the item. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int itemId = WebUtil.intParam(req, "itemId", WebUtil.intParam(req, "id", -1));
        resp.sendRedirect(req.getContextPath() + (itemId > 0 ? "/item?id=" + itemId : "/items"));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = WebUtil.currentUser(req);
        int itemId = WebUtil.intParam(req, "itemId", -1);
        String action = WebUtil.param(req, "action");

        try {
            Item item = itemDao.findById(itemId);
            if (item == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            // AUTHORIZATION: being logged in is not enough - it must be YOUR item
            if (item.getUserId() != user.getId()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You can only manage your own items.");
                return;
            }

            if (action.equals("resolve")) {
                itemDao.markResolved(itemId);
                WebUtil.flash(req, "success", "Marked as resolved.");
                resp.sendRedirect(req.getContextPath() + "/item?id=" + itemId);
            } else if (action.equals("delete")) {
                itemDao.delete(itemId);
                WebUtil.flash(req, "success", "Item deleted.");
                resp.sendRedirect(req.getContextPath() + "/my-items");
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
            }
        } catch (SQLException e) {
            throw new ServletException("Could not update item", e);
        }
    }
}
