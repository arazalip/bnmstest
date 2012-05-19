package com.bourse.nms;

import com.bourse.nms.common.CliUtil;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardThreadExecutor;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.realm.MemoryRealm;
import org.apache.catalina.startup.Embedded;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import java.io.File;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: araz
 * Date: 5/19/12
 * Time: 8:39 PM
 */
public class Launcher {

    private static final Logger log = Logger.getLogger(Launcher.class);

    private final String path;
    private final Embedded container;
    /**
     * The classes directory for the web application being run.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final String classesDir = "target/classes";

    /**
     * Creates a single-webapp configuration to be run in Tomcat on port 8089. If module name does
     * not conform to the 'contextname-webapp' convention, use the two-args constructor.
     *
     * @param contextName without leading slash, for example, "mywebapp"
     */
    public Launcher(String contextName) {
        Assert.isTrue(!contextName.startsWith("/"));
        path = "/" + contextName;
        // create server
        container = new Embedded();
    }

    /**
     * Starts the embedded Tomcat server.
     *
     * @param httpPort     tomcat http connector httpPort
     * @param ajpPort      tomcat ajp connector port
     * @param catalinaHome catalina home path (needs conf/tomcat-users.xml in it)
     * @param appBase      appBase path (tomcat/webapps)
     * @param docBase      docBase path (tomcat/webapps/ROOT)
     * @throws org.apache.catalina.LifecycleException if the server could not be started
     */
    public void run(int httpPort, int ajpPort, String catalinaHome, String docBase, String appBase) throws LifecycleException {

        container.setCatalinaHome(catalinaHome);
        container.setRealm(new MemoryRealm());

        // create webapp loader
        final WebappLoader loader = new WebappLoader(this.getClass().getClassLoader());

        loader.addRepository(new File(classesDir).toURI().toString());

        // create context
        final Context rootContext = container.createContext(path, docBase);
        rootContext.setLoader(loader);
        rootContext.setReloadable(false);

        // create host
        final Host localHost = container.createHost("localhost", appBase);
        localHost.addChild(rootContext);
        localHost.setAutoDeploy(false);
        localHost.setXmlValidation(false);
        localHost.setXmlNamespaceAware(false);

        // create engine
        final Engine engine = container.createEngine();
        engine.setName("localEngine");
        engine.addChild(localHost);
        engine.setDefaultHost(localHost.getName());
        container.addEngine(engine);

        final StandardThreadExecutor executor = new StandardThreadExecutor();
        executor.setName("tomcatThreadPool");
        executor.setMaxThreads(1000);
        executor.setMinSpareThreads(10);
        executor.setNamePrefix("catalina-exec-");
        container.addExecutor(executor);

        // create http connector
        final Connector httpConnector = container.createConnector("0.0.0.0", httpPort, "HTTP/1.1");
        httpConnector.setProperty("connectionTimeout", "20000");
        httpConnector.setRedirectPort(8443);
        httpConnector.setAttribute("executor", executor);
        container.addConnector(httpConnector);

        // create ajp connector
        /*
        Connector ajpConnector = container.createConnector("0.0.0.0", ajpPort, "AJP/1.3");
        ajpConnector.setRedirectPort(8443);
        ajpConnector.setAttribute("executor", executor);
        container.addConnector(ajpConnector);
        */

        container.setAwait(true);

        // start server
        container.start();

        // add shutdown hook to stop server
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                stopContainer();
            }
        });
    }

    /**
     * Stops the embedded Tomcat server.
     */
    public void stopContainer() {
        try {
            if (container != null) {
                container.stop();
            }
        } catch (LifecycleException exception) {
            log.warn("Cannot Stop Tomcat" + exception.getMessage());
        }
    }

    private final static Options options = CliUtil.getOptions();

    public static void main(String[] args) throws Exception {

        log.info("NMS is starting up...");

        //should run maven before run. sample program parameters to run webservice
        //-ch box-webservice/target/classes/tomcat
        //-ab /home/araz/Projects/sms-engine/box-webservice/target
        //-db box-webservice-1.0
        options.addOption("ch", "catalinaHome", true, "specifies catalina home path");
        options.addOption("db", "docBase", true, "specifies docBase path");
        options.addOption("ab", "appBase", true, "specifies appBase path");
        final CommandLine cl = CliUtil.parsArgument(args);

        final Properties prop = new Properties();
        prop.load(Launcher.class.getClassLoader().getResourceAsStream("webservice.properties"));

        final Launcher inst = new Launcher("");
        inst.run(Integer.parseInt(prop.getProperty("tomcat-http-port")), Integer.parseInt(prop.getProperty("tomcat-ajp-port")),
                cl.getOptionValue("catalinaHome"), cl.getOptionValue("docBase"), cl.getOptionValue("appBase"));

        log.info("NMS is up and running...");
    }
}
