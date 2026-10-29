package com.campus.lostfound.listener;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * LISTENER - called by the container whenever an HttpSession is created or destroyed.
 * It keeps a live count of sessions in the ServletContext; footer.jsp shows it
 * as ${applicationScope.activeSessions}.
 */
@WebListener
public class SessionCounterListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        counter(se).incrementAndGet();
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        counter(se).decrementAndGet();
    }

    private AtomicInteger counter(HttpSessionEvent se) {
        return (AtomicInteger) se.getSession().getServletContext().getAttribute("activeSessions");
    }
}
