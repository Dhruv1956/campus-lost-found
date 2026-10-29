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

/** The owner of an item approves or rejects a claim (POST /decide, decision=approve|reject). */
@WebServlet("/decide")
public class ClaimDecisionServlet extends HttpServlet {

    private final ItemDAO itemDao = new ItemDAO();
    private final ClaimDAO claimDao = new ClaimDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = WebUtil.currentUser(req);
        int claimId = WebUtil.intParam(req, "claimId", -1);
        String decision = WebUtil.param(req, "decision");

        try {
            Claim claim = claimDao.findById(claimId);
            if (claim == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            Item item = itemDao.findById(claim.getItemId());
            if (item.getUserId() != user.getId()) {                       // only the item's owner may decide
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only the owner of the item can do this.");
                return;
            }
            if (!"PENDING".equals(claim.getStatus()) || !"OPEN".equals(item.getStatus())) {
                WebUtil.flash(req, "error", "This claim has already been dealt with.");
            } else if (decision.equals("approve")) {
                claimDao.decide(claimId, item.getId(), true);
                WebUtil.flash(req, "success", "Claim approved. The item is marked resolved and "
                        + claim.getClaimerName() + " can now see your contact details.");
            } else if (decision.equals("reject")) {
                claimDao.decide(claimId, item.getId(), false);
                WebUtil.flash(req, "info", "Claim rejected.");
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown decision");
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/item?id=" + item.getId());
        } catch (SQLException e) {
            throw new ServletException("Could not save decision", e);
        }
    }
}
