package com.bourse.nms.log;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * Created by IntelliJ IDEA.
 * User: araz
 * Date: 6/4/12
 * Time: 11:12 PM
 */
public class ActivityLogger {

    /**
     * initialized with size needed for 1 hour of working with 100000 messages/second
     */
    private final BlockingQueue<String> q;
    private static final Logger logger = Logger.getLogger(ActivityLogger.class);

    boolean working =true;

    public ActivityLogger(int activityLoggerQueueSize) {
        this.q = new ArrayBlockingQueue<String>(activityLoggerQueueSize);
    }

    public void init(String fileName) throws IOException {
        final BufferedWriter bw = new BufferedWriter(new FileWriter("/var/log/bnms/" + fileName));
        new Thread(){
            public void run(){
                while (working){
                    try {
                        if(q != null)
                            bw.write(q.take()+"\n");
                            //logger.info(q.take());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
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
