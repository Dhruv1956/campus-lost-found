package com.campus.lostfound.filter;

import com.campus.lostfound.util.WebUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * FILTER - the "security guard" of the application (authentication).
 * Only the URLs listed below need a logged-in user. If there is no "user" attribute
 * in the session, the visitor is sent to the login page; after login he/she is
 * brought back to the page originally requested.
 */
@WebFilter(urlPatterns = {"/items", "/item", "/post", "/my-items", "/claim", "/manage", "/decide"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);           // false = do not create a new session
        boolean loggedIn = session != null && session.getAttribute("user") != null;

        if (loggedIn) {
            chain.doFilter(request, response);                  // allowed - continue to the servlet
            return;
        }

        // Not logged in: remember where he/she wanted to go (only for simple GET page requests)
        if ("GET".equals(req.getMethod())) {
            // full path inside the application (servletPath alone would drop any extra path and so lose where he/she wanted to go)
            String target = req.getRequestURI().substring(req.getContextPath().length());
            if (req.getQueryString() != null) {
                target += "?" + req.getQueryString();
            }
            req.getSession().setAttribute("afterLogin", target);
        }
        WebUtil.flash(req, "info", "Please log in to continue.");
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
