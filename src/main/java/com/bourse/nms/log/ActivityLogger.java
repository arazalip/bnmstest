package com.bourse.nms.log;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * logs orders and trades
 */
public class ActivityLogger {

    private final BlockingQueue<String> q;
    private final String logPath;
    private static final Logger log = Logger.getLogger(ActivityLogger.class);

    boolean working =true;

    public ActivityLogger(int activityLoggerQueueSize, String logPath) {
        this.q = new ArrayBlockingQueue<>(activityLoggerQueueSize);
        this.logPath = logPath;
    }

    /**
     * initializes activity logger with a file that will be creates in log path
     * @param fileName file name
     * @throws IOException
     */
    public void init(String fileName) throws IOException {
        final BufferedWriter bw = new BufferedWriter(new FileWriter(logPath + fileName));
        new Thread(){
            public void run(){
                while (working){
                    try {
                        if(q != null)
                            bw.write(q.take()+"\n");
                    } catch (InterruptedException | IOException e) {
                        log.warn("exception on writing activity log", e);
                    }
                }
            }
        }.start();
    }

    public void log(String msg) {
        q.add(msg);
    }

}
