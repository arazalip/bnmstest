package com.bourse.nms.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by IntelliJ IDEA.
 * User: araz
 * Date: 6/4/12
 * Time: 11:12 PM
 */
public class ActivityLogger {

    private static final Logger logger = LoggerFactory.getLogger(ActivityLogger.class);

    public void log(String msg) {
        logger.info(msg);
    }

}
