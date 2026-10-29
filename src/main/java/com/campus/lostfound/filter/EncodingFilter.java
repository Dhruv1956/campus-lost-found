package com.campus.lostfound.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * FILTER - runs before EVERY request ("/*").
 * Makes sure form data and pages are read/written as UTF-8,
 * so names and descriptions with any characters are not garbled.
 * It also tells the browser NOT to cache pages, so after logout the Back button
 * cannot show a protected page from the browser's cache (stylesheet is still cached).
 */
@WebFilter("/*")
public class EncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        String path = ((HttpServletRequest) req).getRequestURI();
        if (!path.contains("/css/")) {
            HttpServletResponse r = (HttpServletResponse) resp;
            r.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            r.setHeader("Pragma", "no-cache");
            r.setDateHeader("Expires", 0);
        }
        chain.doFilter(req, resp);          // pass the request on to the next filter / the servlet
    }
}
