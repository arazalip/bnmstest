package com.bourse.nms.log;

import org.apache.log4j.Logger;


/**
 * Created by IntelliJ IDEA.
 * User: araz
 * Date: 6/4/12
 * Time: 11:12 PM
 */
public class ActivityLogger {

    private static final Logger logger = Logger.getLogger(ActivityLogger.class);

    public void log(String msg) {
        logger.info(msg);
    }

}
