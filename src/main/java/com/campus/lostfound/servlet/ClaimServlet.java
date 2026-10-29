package com.campus.lostfound.servlet;

import com.campus.lostfound.dao.ClaimDAO;
import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.model.Claim;
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

/** A logged-in user sends a claim / contact message about someone else's item (POST /claim). */
@WebServlet("/claim")
public class ClaimServlet extends HttpServlet {

    private final ItemDAO itemDao = new ItemDAO();
    private final ClaimDAO claimDao = new ClaimDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = WebUtil.currentUser(req);
        int itemId = WebUtil.intParam(req, "itemId", -1);
        String message = WebUtil.param(req, "message");
        String back = req.getContextPath() + "/item?id=" + itemId;

        try {
            Item item = itemDao.findById(itemId);
            if (item == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (item.getUserId() == user.getId()) {
                WebUtil.flash(req, "error", "You cannot claim your own item.");
            } else if (!"OPEN".equals(item.getStatus())) {
                WebUtil.flash(req, "error", "This item is already resolved.");
            } else if (message.length() < 5 || message.length() > 500) {
                WebUtil.flash(req, "error", "Your message must be 5 to 500 characters.");
            } else if (claimDao.findByItemAndClaimer(itemId, user.getId()) != null) {
                WebUtil.flash(req, "error", "You have already sent a claim for this item.");
            } else {
                Claim c = new Claim();
                c.setItemId(itemId);
                c.setClaimerId(user.getId());
                c.setMessage(message);
                claimDao.insert(c);
                WebUtil.flash(req, "success", "Your message was sent to the owner. You will see their contact details once they approve it.");
            }
        } catch (SQLException e) {
            throw new ServletException("Could not save claim", e);
        }
        resp.sendRedirect(back);
    }
}
