package com.bourse.nms.engine;

import com.bourse.nms.common.NMSException;
import com.bourse.nms.entity.Order;
import com.bourse.nms.entity.Order.OrderSide;
import com.bourse.nms.entity.Settings;
import com.bourse.nms.log.ActivityLogger;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * engine implementation
 */
public class EngineImpl implements Engine {

    private final static Logger log = Logger.getLogger(EngineImpl.class);

    /**
     * logs put orders and trades
     */
    @Autowired
    private ActivityLogger acLog;
    /**
     * system settings
     */
    @Autowired
    private Settings settings;
    /**
     * a map containing buy queues by their stock id
     */
    private final Map<Integer, PriorityBlockingQueue<Order>> buyQueues = Collections.synchronizedMap(new HashMap<Integer, PriorityBlockingQueue<Order>>());
    /**
     * to bother less when ui asks of sizes
     */
    private final AtomicInteger buyQueuesSizes = new AtomicInteger(0);
    /**
     * a map containing sell queues by their stock id
     */
    private final Map<Integer, PriorityBlockingQueue<Order>> sellQueues = Collections.synchronizedMap(new HashMap<Integer, PriorityBlockingQueue<Order>>());
    /**
     * to bother less when ui asks of sizes
     */
    private final AtomicInteger sellQueuesSizes = new AtomicInteger(0);
    /**
     * threads that will do trading. a separate thread for each stock
     */
    private final Map<Integer, TradingThread> tradingThreads = new HashMap<>();
    /**
     * shows how many orders have been put in engine queues
     */
    private final AtomicInteger orderPutCounter = new AtomicInteger(0);
    /**
     * increments by each trade that is done
     */
    private final AtomicInteger tradeCounter = new AtomicInteger(0);

    /**
     * total trade cost
     */
    private final AtomicLong totalTradesCost = new AtomicLong(0);

    /**
     * engine previous state is stored for pause/resume implementation
     */
    private Settings.EngineStatus prevState = null;

    private final AtomicInteger minimumPutOrderPerSeconds = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger maximumPutOrderPerSeconds = new AtomicInteger(0);
    private final AtomicInteger meanPutOrderPerSeconds = new AtomicInteger(0);

    private final AtomicInteger minimumTradePerSeconds = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger maximumTradePerSeconds = new AtomicInteger(0);
    private final AtomicInteger meanTradePerSeconds = new AtomicInteger(0);

    private Timer statsTimer = new Timer();

    /**
     * queues initial size
     */
    private final int queuesInitialSize;

    /**
     * default constructor
     * @param queuesInitialSize should be set regarding JVM memory and number of stocks.
     * with 4096MB of JVM memory, 48 stocks and queuesInitialSize=3000000 OutOfMemory is not happening
     */
    public EngineImpl(int queuesInitialSize) {
        this.queuesInitialSize = queuesInitialSize;
    }

    @Override
    public void startPreOpening() {
        //restart();
        statsTimer.schedule(new TimerTask() {
            int lastPutOrderCount = 0;
            int lastTradeCount = 0;
            @Override
            public void run() {
                if(settings == null || settings.getStatus() == null){
                    return;
                }
                if(settings.getStatus().equals(Settings.EngineStatus.PRE_OPENING) ||
                        settings.getStatus().equals(Settings.EngineStatus.TRADING)){
                    final int putOrderDiff = orderPutCounter.get() - lastPutOrderCount;
                    if(putOrderDiff > 0 && putOrderDiff < minimumPutOrderPerSeconds.get()){
                        minimumPutOrderPerSeconds.set(putOrderDiff);
                    }
                    if(putOrderDiff > maximumPutOrderPerSeconds.get()){
                        maximumPutOrderPerSeconds.set(putOrderDiff);
                    }
                    if(putOrderDiff > 0){
                        if(meanPutOrderPerSeconds.get() == 0){
                            meanPutOrderPerSeconds.set(putOrderDiff);
                        }else{
                            meanPutOrderPerSeconds.set((putOrderDiff + meanPutOrderPerSeconds.get())/2);
                        }
                    }
                    lastPutOrderCount += putOrderDiff;
                }
                if(settings.getStatus().equals(Settings.EngineStatus.TRADING)){
                    final int tradingDiff = tradeCounter.get() - lastTradeCount;
                    if(tradingDiff > 0 && tradingDiff < minimumTradePerSeconds.get()){
                        minimumTradePerSeconds.set(tradingDiff);
                    }
                    if(tradingDiff > maximumTradePerSeconds.get()){
                        maximumTradePerSeconds.set(tradingDiff);
                    }
                    if(tradingDiff > 0){
                        if(meanTradePerSeconds.get() == 0)
                            meanTradePerSeconds.set(tradingDiff);
                        else
                            meanTradePerSeconds.set((tradingDiff + meanTradePerSeconds.get())/2);
                    }
                    lastTradeCount += tradingDiff;
                }
            }
        }, 0, 1000);

        settings.setStatus(Settings.EngineStatus.PRE_OPENING);
    }

    @Override
    public void putOrder(Order order, OrderSide orderSide, int stockId, int tradePrice) throws NMSException {
        final Settings.EngineStatus state = settings.getStatus();
        if (!state.equals(Settings.EngineStatus.PRE_OPENING) && !state.equals(Settings.EngineStatus.TRADING)) {
            throw new NMSException(NMSException.ErrorCode.INVALID_STATE_FOR_PUT_ORDER, "put order is not functional when engine is in state: " + state.name());
        }
        if (orderSide.equals(OrderSide.BUY)) {
            if (!buyQueues.containsKey(stockId)) {
                buyQueues.put(stockId, new PriorityBlockingQueue<>(queuesInitialSize, new Comparator<Order>() {
                    @Override
                    public int compare(Order o1, Order o2) {
                        return o2.compareTo(o1);
                    }
                }));
            }
            buyQueues.get(stockId).add(order);
            buyQueuesSizes.incrementAndGet();
        } else {
            if (!sellQueues.containsKey(stockId)) {
                sellQueues.put(stockId, new PriorityBlockingQueue<Order>(queuesInitialSize));
            }
            sellQueues.get(stockId).add(order);
            sellQueuesSizes.incrementAndGet();
        }

        orderPutCounter.incrementAndGet();
        acLog.log("O " + orderSide + "," + stockId + "," + order.toString());
        if (!tradingThreads.containsKey(stockId)) {
            tradingThreads.put(stockId, new TradingThread(stockId, tradePrice));
        }

        if(settings.getStatus().equals(Settings.EngineStatus.TRADING)){
            final TradingThread stockTradingThread = tradingThreads.get(stockId);
            try{
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (stockTradingThread) {//thread can check itself to see if new orders have come, if this synchronize affects efficiency
                stockTradingThread.notify();
            }
            }catch (NullPointerException e){
                log.warn("NullPointerException on thread notify",e);
            }
        }
    }

    @Override
    public void startTrading() {
        settings.setStatus(Settings.EngineStatus.TRADING);
        for (TradingThread tt : tradingThreads.values()) {
            try{
                if(tt.isAlive()){
                    log.warn("thread is alive! " + tt.toString());
                    continue;
                }
                log.info("starting thread: " + tt);
                tt.start();
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void pause() throws NMSException {
        prevState = settings.getStatus();
        settings.setStatus(Settings.EngineStatus.PAUSED);
    }

    @Override
    public void stop() throws NMSException {
        int countBuy = 0;
        int countSell = 0;
        for (PriorityBlockingQueue<Order> buyQueue : buyQueues.values()) {
            countBuy += buyQueue.size();
        }
        for (PriorityBlockingQueue<Order> sellQueue : sellQueues.values()) {
            countSell += sellQueue.size();
        }
        log.info("putOrderCount: " + orderPutCounter + ", tradeCount: " + tradeCounter + ", queues count buy: " + countBuy + ", sell: " + countSell);
        settings.setStatus(Settings.EngineStatus.FINISHED);
        acLog.closeWriters();
    }

    @Override
    public void resume() {
        if (prevState != null)
            settings.setStatus(prevState);
    }

    @Override
    public void restart() {
        log.info("engine restarted");
        statsTimer.cancel();
        statsTimer = new Timer();
        buyQueues.clear();
        sellQueues.clear();
        tradingThreads.clear();
        orderPutCounter.set(0);
        tradeCounter.set(0);
        sellQueuesSizes.set(0);
        buyQueuesSizes.set(0);
        totalTradesCost.set(0);
        minimumPutOrderPerSeconds.set(Integer.MAX_VALUE);
        maximumPutOrderPerSeconds.set(0);
        meanPutOrderPerSeconds.set(0);
        minimumTradePerSeconds.set(Integer.MAX_VALUE);
        maximumTradePerSeconds.set(0);
        meanTradePerSeconds.set(0);
        settings.setStatus(Settings.EngineStatus.WAITING);
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
        return buyQueuesSizes.get();
    }

    @Override
    public int getSellQueueSize() {
        return sellQueuesSizes.get();
    }

    @Override
    public int getMeanPutOrder() {
        return meanPutOrderPerSeconds.get();
    }

    @Override
    public int getMinPutOrder() {
        return minimumPutOrderPerSeconds.get();
    }

    @Override
    public int getMaxPutOrder() {
        return maximumPutOrderPerSeconds.get();
    }

    @Override
    public int getMeanTrade() {
        return meanTradePerSeconds.get();
    }

    @Override
    public int getMinTrade() {
        return minimumTradePerSeconds.get();
    }

    @Override
    public int getMaxTrade() {
        return maximumTradePerSeconds.get();
    }

    @Override
    public long getTradesCost() {
        return totalTradesCost.get();
    }


    public class TradingThread extends Thread {
        private final int stockId;
        private final int tradePrice;
        public TradingThread(int stockId, int tradePrice) {
            this.stockId = stockId;
            this.tradePrice = tradePrice;
        }

        public void run() {
            while (settings.getStatus().equals(Settings.EngineStatus.TRADING)
                    || settings.getStatus().equals(Settings.EngineStatus.PAUSED)) {
                if(settings.getStatus().equals(Settings.EngineStatus.PAUSED)){
                    while(settings.getStatus().equals(Settings.EngineStatus.PAUSED)){
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            log.warn("exception on thread wait while paused");
                        }
                    }
                }
                final PriorityBlockingQueue<Order> buyQueue = buyQueues.get(stockId);
                final PriorityBlockingQueue<Order> sellQueue = sellQueues.get(stockId);
                final Order buyOrder;
                final Order sellOrder;
                try {
                    buyOrder = buyQueue.take();
                    sellOrder = sellQueue.take();
                } catch (InterruptedException e) {
                    log.warn("InterruptedException on trading thread wait for empty queue", e);
                    return;
                }
                if (buyOrder.getPrice() < sellOrder.getPrice()) {
                    log.debug("trade could not be done with queue heads. putOrderCount:" + orderPutCounter + ", buy queue size:" + buyQueue.size() + ", sell queue size:" +sellQueue.size());
                    try {
                        buyQueue.add(buyOrder);
                        sellQueue.add(sellOrder);
                        synchronized (this) {//same as notify
                            this.wait();
                        }
                    } catch (InterruptedException e) {
                        log.warn("InterruptedException on trading thread wait while no more trades could be made", e);
                    }
                } else {
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
                    totalTradesCost.addAndGet(Math.min(buyOrder.getTotalQuantity(), sellOrder.getTotalQuantity()) * tradePrice);
                    acLog.log("T:" + stockId + " b:" + buyOrder + " s:" + sellOrder);
                    tradeCounter.incrementAndGet();
                    log.debug("sell queue size: " + sellQueue.size() + ", buy queue size: " + buyQueue.size() + ", putOrderCount: " + orderPutCounter.get() + ", tradeCount: " + tradeCounter.get());
                }
            }
        }

    }

}
