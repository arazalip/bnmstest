package com.bourse.nms.engine;

import com.bourse.nms.common.NMSException;
import com.bourse.nms.entity.Order;
import com.bourse.nms.entity.Order.OrderSide;

/**
 * Created by IntelliJ IDEA.
 * User: araz
 * Date: 6/1/12
 * Time: 2:02 PM
 */
public interface Engine {

    public void startPreOpening();

    public void putOrder(Order order, OrderSide orderSide, int stockId) throws NMSException;

    public void startTrading();

    public void pause();

    public void stop();

    public void resume();

    public int getPutOrderCount();

    public int getTradeCount();

    public int getBuyQueueSize();

    public int getSellQueueSize();

}
