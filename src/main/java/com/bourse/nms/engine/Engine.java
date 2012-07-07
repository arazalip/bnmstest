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
    public void putOrder(Order order, OrderSide orderSide, int stockId, int tradeCost) throws NMSException;

    /**
     * starts trading process
     */
    public void startTrading();

    /**
     * pauses process
     */
    public void pause() throws NMSException;

    /**
     * stops process
     */
    public void stop() throws NMSException;

    /**
     * resumes a paused process
     */
    public void resume();

    /**
     * restarts engine
     */
    public void restart();

    public int getPutOrderCount();
    public int getTradeCount();
    public int getBuyQueueSize();
    public int getSellQueueSize();
    public int getMeanPutOrder();
    public int getMinPutOrder();
    public int getMaxPutOrder();
    public int getMeanTrade();
    public int getMinTrade();
    public int getMaxTrade();
    public long getTradesCost();


}
