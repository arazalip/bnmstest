package com.bourse.nms.engine;

import com.bourse.nms.common.NMSException;
import com.bourse.nms.entity.Order;
import com.bourse.nms.entity.Order.OrderSide;
import com.bourse.nms.log.ActivityLogger;
import org.apache.log4j.Logger;


import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: araz
 * Date: 6/4/12
 * Time: 8:39 PM
 */
public class EngineImpl implements Engine {

    private final ActivityLogger acLog;
    private final static Logger log = Logger.getLogger(EngineImpl.class);
    private final Map<Integer, PriorityBlockingQueue<Order>> buyQueues = new HashMap<>();
    private final Map<Integer, PriorityBlockingQueue<Order>> sellQueues = new HashMap<>();
    private final Map<Integer, TradingThread> tradingThreads = new HashMap<>();
    private final AtomicInteger orderPutCounter = new AtomicInteger(0);
    private final AtomicInteger tradeCounter = new AtomicInteger(0);
    private State state = State.NOT_STARTED;
    private State prevState = null;

    public EngineImpl(ActivityLogger acLog) {
        this.acLog = acLog;
    }

    public enum State {
        NOT_STARTED, PRE_OPENING, TRADING, PAUSED, FINISHED
    }

    @Override
    public void startPreOpening() {
        state = State.PRE_OPENING;
    }

    @Override
    public void putOrder(Order order, OrderSide orderSide, int stockId) throws NMSException {
        if (!state.equals(State.PRE_OPENING) && !state.equals(State.TRADING)) {
            throw new NMSException(NMSException.ErrorCode.INVALID_STATE_FOR_PUT_ORDER, "put order is not functional when engine is in state: " + state.name());
        }
        if (orderSide.equals(OrderSide.BUY)) {
            if (!buyQueues.containsKey(stockId)) {
                buyQueues.put(stockId, new PriorityBlockingQueue<Order>(3000000, new Comparator<Order>() {
                    @Override
                    public int compare(Order o1, Order o2) {
                        return o2.compareTo(o1);
                    }
                }));
            }
            buyQueues.get(stockId).add(order);
        } else {
            if (!sellQueues.containsKey(stockId)) {
                sellQueues.put(stockId, new PriorityBlockingQueue<Order>(3000000));
            }
            sellQueues.get(stockId).add(order);
        }

        orderPutCounter.incrementAndGet();
        acLog.log("O " + orderSide + "," + stockId + "," + order.toString());
        if (!tradingThreads.containsKey(stockId)) {
            tradingThreads.put(stockId, new TradingThread(stockId));
        }
        final TradingThread stockTradingThread = tradingThreads.get(stockId);
        synchronized (stockTradingThread) {//thread can check itself to see if new orders have come, if this synchronize affects efficiency
            stockTradingThread.notify();
        }
    }

    @Override
    public void startTrading() {
        state = State.TRADING;
        for (TradingThread tt : tradingThreads.values()) {
            tt.start();
        }
    }

    @Override
    public void pause() {
        prevState = state;
        state = State.PAUSED;
    }

    @Override
    public void stop() {
        int countBuy = 0;
        int countSell = 0;
        for (PriorityBlockingQueue<Order> buyQueue : buyQueues.values()) {
            countBuy += buyQueue.size();
        }
        for (PriorityBlockingQueue<Order> sellQueue : sellQueues.values()) {
            countSell += sellQueue.size();
        }
        log.info("putOrderCount: " + orderPutCounter + ", tradeCount: " + tradeCounter + ", queues count buy: " + countBuy + ", sell: " + countSell);
        state = State.FINISHED;
        buyQueues.clear();
        sellQueues.clear();
        tradingThreads.clear();
        orderPutCounter.set(0);
        tradeCounter.set(0);
    }

    @Override
    public void resume() {
        if (prevState != null)
            state = prevState;
    }

    @Override
    public int getPutOrderCount() {
        return orderPutCounter.intValue();
    }

    @Override
    public int getTradeCount() {
        return tradeCounter.intValue();
    }

    @Override
    public int getBuyQueueSize() {
        int result = 0;
        for (Queue q : buyQueues.values()) {
            result += q.size();
        }
        return result;
    }

    @Override
    public int getSellQueueSize() {
        int result = 0;
        for (Queue q : sellQueues.values()) {
            result += q.size();
        }
        return result;
    }

    public class TradingThread extends Thread {
        private final int stockId;

        public TradingThread(int stockId) {
            this.stockId = stockId;
        }

        public void run() {
            while (state.equals(EngineImpl.State.TRADING)) {
                final PriorityBlockingQueue<Order> buyQueue = buyQueues.get(stockId);
                final PriorityBlockingQueue<Order> sellQueue = sellQueues.get(stockId);
                if (buyQueue.isEmpty() || sellQueue.isEmpty()) {
                    try {
                        log.debug("sell or buy queue is empty. sellQueue: " + sellQueue.size() + ", buyQueue: " + buyQueue.size());
                        synchronized (this) {//same as notify
                            this.wait();
                            continue;
                        }
                    } catch (InterruptedException e) {
                        //log.warn("InterruptedException on trading thread wait while queues are empty", e);
                        log.warn("InterruptedException on trading thread wait while queues are empty", e);
                    }
                }
                final Order buyOrder = buyQueue.poll();
                final Order sellOrder = sellQueue.poll();
                if (buyOrder.getPrice() < sellOrder.getPrice()) {
                    //log.warn("trade could not be done with queue heads. putOrderCount:" + orderPutCounter + ", buy queue size:" + buyQueue.size() + ", sell queue size:" +sellQueue.size());
                    //acLog.log("trade could not be done with queue heads");
                    try {
                        synchronized (this) {//same as notify
                            this.wait();
                        }
                    } catch (InterruptedException e) {
                        log.warn("InterruptedException on trading thread wait while no more trades could be made", e);
                    }
                } else {
                    //it would be better to remove final from BuyOrder's totalQuantity or  make it atomic,
                    // so it shouldn't be removed and replaced in the queue
                    if (buyOrder.getTotalQuantity() > sellOrder.getTotalQuantity()) {
                        buyQueue.add(new Order((buyOrder.getTotalQuantity() - sellOrder.getTotalQuantity()),
                                buyOrder.getSubscriberId(),
                                buyOrder.getPrice(),
                                buyOrder.getSubscriberPriority()));
                    } else if (buyOrder.getTotalQuantity() < sellOrder.getTotalQuantity()) {
                        sellQueue.add(new Order((sellOrder.getTotalQuantity() - buyOrder.getTotalQuantity()),
                                sellOrder.getSubscriberId(),
                                sellOrder.getPrice(),
                                sellOrder.getSubscriberPriority()));

                    }
                    acLog.log("T:" + stockId + " b:" + buyOrder + " s:" + sellOrder);
                    tradeCounter.incrementAndGet();
                    //log.debug("sell queue size: " + sellQueue.size() + ", buy queue size: " + buyQueue.size() + ", putOrderCount: " + orderPutCounter.get() + ", tradeCount: " + tradeCounter.get());
                }
            }
        }

    }

    public static void main(String[] args) throws NMSException, InterruptedException {

/*
        Engine e = new EngineImpl(new ActivityLogger());
        log.debug("system millis: " + System.currentTimeMillis());
        e.startPreOpening();
        log.debug("pre opening started. --" + "system millis: " + System.currentTimeMillis());
        final int totalOrderCount = 30000000;
        putOrders(e, totalOrderCount);
        Thread.sleep(30 * 1000);
        log.debug(totalOrderCount + "orders put. --" + "system millis: " + System.currentTimeMillis());
        e.startTrading();
        log.debug("trading started. --" + "system millis: " + System.currentTimeMillis());
        putOrders(e, totalOrderCount);
        Thread.sleep(300 * 1000);
        log.debug(totalOrderCount + "orders put. --" + "system millis: " + System.currentTimeMillis());
        e.stop();
        log.debug("stopped. --" + "system millis: " + System.currentTimeMillis());
    }

    public static void putOrders(final Engine e, final int totalCount) throws NMSException {
        final Random r = new Random();
        Thread t1 = new Thread() {
            public void run() {
                for (int i = 0; i < totalCount / 2; i++) {
                    try {
                        e.putOrder(new Order(r.nextInt(100), (byte) 1, r.nextInt(1000), 1), OrderSide.BUY, r.nextInt(10) + 1);
                    } catch (NMSException e1) {
                        log.warn("NMSException: " + e);
                    }
                }
            }
        };
        t1.start();

        Thread t2 = new Thread() {
            public void run() {
                for (int i = 0; i < totalCount / 2; i++) {
                    try {
                        e.putOrder(new Order(r.nextInt(100), (byte) 1, r.nextInt(1000), 1), OrderSide.SELL, r.nextInt(10) + 1);
                    } catch (NMSException e1) {
                        log.warn("NMSException: " + e);
                    }
                }
            }
        };
        t2.start();
*/

    }
}
