package com.bourse.nms.engine;

import com.bourse.nms.entity.Order;
import com.bourse.nms.log.ActivityLogger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by IntelliJ IDEA.
 * User: araz
 * Date: 6/4/12
 * Time: 8:39 PM
 */
public class EngineImpl implements Engine {

    @Autowired
    private ActivityLogger aclog;

    @Override
    public void startPreOpening() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void putOrder(Order order) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void startTrading() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
