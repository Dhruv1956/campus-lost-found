package com.campus.lostfound.util;

import com.campus.lostfound.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** Small helpers shared by the servlets, so each servlet stays short. */
public final class WebUtil {

    private WebUtil() { }

    /** The logged-in user, or null if nobody is logged in. */
    public static User currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (User) session.getAttribute("user");
    }

    /**
     * Stores a one-time message ("flash message") in the session.
     * header.jsp shows it on the next page and then removes it.
     * type is "success", "error" or "info" (used as a CSS class).
     */
    public static void flash(HttpServletRequest req, String type, String message) {
        HttpSession session = req.getSession();
        session.setAttribute("flash", message);
        session.setAttribute("flashType", type);
    }

    /** Reads an int request parameter; returns defaultValue if missing or not a number. */
    public static int intParam(HttpServletRequest req, String name, int defaultValue) {
        try {
            return Integer.parseInt(req.getParameter(name).trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /** Request parameter, trimmed; never null. */
    public static String param(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        return value == null ? "" : value.trim();
    }

    /** Forwards to a JSP stored under /WEB-INF/views (browsers cannot open those directly). */
    public static void forward(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + view).forward(req, resp);
    }
}
