package com.bourse.nms.engine;

import com.bourse.nms.common.NMSException;
import com.bourse.nms.entity.Order;
import com.bourse.nms.entity.Order.OrderSide;

/**
 * engine core of the system
 */
public interface Engine {

    /**
     * starts pre-opening process
     */
    public void startPreOpening();

    /**
     * puts order in queues
     * @param order the order object
     * @param orderSide order side (BUY/SELL)
     * @param stockId order stock id
     * @throws NMSException
     */
    public void putOrder(Order order, OrderSide orderSide, int stockId) throws NMSException;

    /**
     * starts trading process
     */
    public void startTrading();

    /**
     * pauses process
     */
    public void pause();

    /**
     * stops process
     */
    public void stop();

    /**
     * resumes a paused process
     */
    public void resume();

    /**
     * returns put order count
     * @return put order count
     */
    public int getPutOrderCount();

    /**
     * returns trade count
     * @return trade count
     */
    public int getTradeCount();

    /**
     * returns all buy queues sizes
     * @return buy queue sizes
     */
    public int getBuyQueueSize();

    /**
     * returns all sell queue sizes
     * @return sell queues sizes
     */
    public int getSellQueueSize();

}
