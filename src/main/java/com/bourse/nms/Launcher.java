package com.bourse.nms;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * software main. application launcher.
 * starts embedded tomcat and initializes spring context.
 */
public class Launcher {

    private static final Logger log = Logger.getLogger(Launcher.class);

    public static void main(final String[] args) throws ServletException, LifecycleException, URISyntaxException, IOException {
        final Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);  // Default connector
        addConnector(8082, false, tomcat, null);
        final URL location = new File("target/bnmstest-1.0").toURI().toURL();
        log.debug("Using webapp at " + location.toExternalForm());
        tomcat.addWebapp("/", location.toURI().getPath());
        tomcat.start();
        tomcat.getServer().await();
    }


    private static void addConnector(final int port, final boolean https, final Tomcat tomcat, final File[] certificateStores) throws IOException {
        final Connector connector = new Connector();
        connector.setScheme((https) ? "https" : "http");
        connector.setPort(port);
        connector.setProperty("maxPostSize", "0");  // unlimited
        connector.setProperty("xpoweredBy", "true");
        if (https) {
            connector.setSecure(true);
            connector.setProperty("SSLEnabled", "true");
            connector.setProperty("keyPass", "123456");
            connector.setProperty("keystoreFile", certificateStores[0].getCanonicalPath());
            connector.setProperty("keystorePass", "123456");
            connector.setProperty("truststoreFile", certificateStores[1].getCanonicalPath());
            connector.setProperty("truststorePass", "123456");
        }
        tomcat.getService().addConnector(connector);
    }
}
