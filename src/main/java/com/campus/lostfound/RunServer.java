package com.campus.lostfound;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

/**
 * Convenience launcher so the project runs with ONE command, without installing Tomcat:
 *
 *      mvn compile exec:java
 *
 * It starts an embedded Tomcat on port 8080 and deploys src/main/webapp (JSPs, CSS, web.xml)
 * together with the compiled classes from target/classes.
 *
 * The same code also runs on a normal Tomcat 10.1: build the WAR with "mvn package"
 * and copy target/campus-lost-found.war into Tomcat's webapps folder. This class is not used then.
 */
public class RunServer {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (System.getenv("PORT") != null) {
            port = Integer.parseInt(System.getenv("PORT"));
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(new File(System.getProperty("java.io.tmpdir"), "campus-tomcat").getAbsolutePath());
        tomcat.setPort(port);
        tomcat.getConnector();                                   // creates the HTTP connector

        // "" = deploy at the root, so the site is http://localhost:8080/
        File webappDir = new File("src/main/webapp");
        Context context = tomcat.addWebapp("", webappDir.getAbsolutePath());

        // "mvn exec:java" loads our libraries in its own class loader. The web application must
        // use that same loader as its parent, otherwise it cannot find Tomcat/JSTL/H2 classes.
        context.setParentClassLoader(RunServer.class.getClassLoader());

        // tell Tomcat where the compiled servlets/filters/listeners are
        File classesDir = new File("target/classes");
        WebResourceRoot resources = new StandardRoot(context);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                classesDir.getAbsolutePath(), "/"));
        context.setResources(resources);

        tomcat.start();

        // If the web application failed to start (e.g. database error), say so instead of
        // printing the success banner.
        if (!context.getState().isAvailable()) {
            System.err.println();
            System.err.println("*** The web application FAILED to start. Read the SEVERE messages above. ***");
            tomcat.stop();
            tomcat.destroy();
            System.exit(1);
        }

        System.out.println();
        System.out.println("=================================================");
        System.out.println("  Campus Lost & Found is running");
        System.out.println("  Open:  http://localhost:" + port + "/");
        System.out.println("  Demo login:  demo@campus.edu / demo123");
        System.out.println("  Stop with Ctrl + C");
        System.out.println("=================================================");
        tomcat.getServer().await();
    }
}
