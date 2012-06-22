package com.bourse.nms.log;

import org.apache.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * Created by IntelliJ IDEA.
 * User: araz
 * Date: 6/4/12
 * Time: 11:12 PM
 */
public class ActivityLogger {

    private static final Logger logger = Logger.getLogger(ActivityLogger.class);
    private final BlockingQueue<String> q = new ArrayBlockingQueue<String>(50000000);

    public void init(int queueSize){
        //q = new ArrayBlockingQueue<String>(queueSize);
    }

    public ActivityLogger(){

            new Thread(){
                public void run(){
                    while (true){
                        try {
                            if(q != null)
                                logger.info(q.take());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();


    }

    public void log(String msg) {
        q.add(msg);
    }

}
