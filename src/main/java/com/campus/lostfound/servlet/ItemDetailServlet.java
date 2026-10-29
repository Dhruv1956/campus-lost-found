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

/**
 * Detail page of one item: /item?id=5
 * What the page shows depends on WHO is looking at it:
 *   - the owner         -> list of claims with Approve / Reject buttons
 *   - another user      -> a claim form, or the status of his/her own claim
 *   - an approved claimer sees the owner's phone and e-mail (contact details stay private otherwise)
 *   - a visitor who is not logged in -> a "log in to contact" message
 */
@WebServlet("/item")
public class ItemDetailServlet extends HttpServlet {

    private final ItemDAO itemDao = new ItemDAO();
    private final ClaimDAO claimDao = new ClaimDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int id = WebUtil.intParam(req, "id", -1);
        try {
            Item item = itemDao.findById(id);
            if (item == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Item not found");
                return;
            }
            User user = WebUtil.currentUser(req);
            boolean isOwner = user != null && user.getId() == item.getUserId();

            req.setAttribute("item", item);
            req.setAttribute("isOwner", isOwner);

            if (isOwner) {
                req.setAttribute("claims", claimDao.findByItem(id));
            } else if (user != null) {
                Claim myClaim = claimDao.findByItemAndClaimer(id, user.getId());
                req.setAttribute("myClaim", myClaim);
                req.setAttribute("showOwnerContact", myClaim != null && "APPROVED".equals(myClaim.getStatus()));
            }
        } catch (SQLException e) {
            throw new ServletException("Could not load item", e);
        }
        WebUtil.forward(req, resp, "item-detail.jsp");
    }
}
