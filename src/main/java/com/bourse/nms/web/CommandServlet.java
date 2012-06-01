package com.bourse.nms.web;

import com.bourse.nms.common.NMSException;
import com.bourse.nms.generator.Generator;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: araz
 * Date: 6/1/12
 * Time: 5:29 PM
 */
public class CommandServlet extends HttpServlet {

    private final Logger log = Logger.getLogger(CommandServlet.class);

    private Generator generator;
    public void init(){
        log.info("Command Servlet Init...");
        final ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        generator = (Generator) context.getBean("generator");
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        final String action = req.getParameter("action");

        switch (action){
            case "start":
                try {
                    generator.startProcess();
                    resp.getWriter().write(new AjaxResponse(0, "OK").toString());
                } catch (NMSException e) {
                    log.warn("exception on sending start command", e);
                    resp.getWriter().write(new AjaxResponse(e).toString());
                }
                break;
            case "stop":
                try {
                    generator.stopProcess();
                    resp.getWriter().write(new AjaxResponse(0, "OK").toString());
                } catch (NMSException e) {
                    log.warn("exception on sending stop command", e);
                    resp.getWriter().write(new AjaxResponse(e).toString());
                }
                break;
            case "restart":
                try {
                    generator.restartProcess();
                    resp.getWriter().write(new AjaxResponse(0, "OK").toString());
                } catch (NMSException e) {
                    log.warn("exception on sending restart command", e);
                    resp.getWriter().write(new AjaxResponse(e).toString());
                }
                break;
            case "pause":
                try {
                    generator.pauseProcess();
                    resp.getWriter().write(new AjaxResponse(0, "OK").toString());
                } catch (NMSException e) {
                    log.warn("exception on sending pause command", e);
                    resp.getWriter().write(new AjaxResponse(e).toString());
                }
                break;
            case "subFile":
                log.info("got sub file");
                break;
            default:
                log.warn("unknown command action: " + action);
                break;
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {

    }
}