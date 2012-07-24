package com.bourse.nms.generator;

import com.bourse.nms.common.NMSException;
import com.bourse.nms.engine.Engine;
import com.bourse.nms.entity.Order;
import com.bourse.nms.entity.Order.OrderSide;
import com.bourse.nms.entity.Settings;
import com.bourse.nms.entity.Subscriber;
import com.bourse.nms.entity.Symbol;
import com.bourse.nms.log.ActivityLogger;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;

/**
 * generator implementation
 */
public class GeneratorImpl implements Generator {

    private static Logger log = Logger.getLogger(GeneratorImpl.class);

    @Autowired
    private Engine engine;
    @Autowired
    private ActivityLogger activityLogger;
    @Autowired
    private Settings settings;

    private volatile boolean working;

    private int preOpeningTime;   //in minutes
    private int tradingTime;  //in minutes
    private int buyOrdersCount;
    private int sellOrdersCount;
    private int preOpeningBuyOrdersCount;
    private int preOpeningSellOrdersCount;
    private int matchPercent;
    private ArrayList<Subscriber> customers;
    private Map<Integer, Symbol> symbols;
    private ArrayList<Integer> stockIds;
    private int stocksCount;

    private Thread processStarter;

    public Order randomOrder(OrderSide orderSide, int stockId) {
        final Random random = new Random();
        final boolean isBuy = orderSide.equals(OrderSide.BUY);

        //decide symbol
        final Symbol symbol = getSymbolWithId(stockId);

        //decide quantity
        final int quantityRange = isBuy ? symbol.getCountRangeForBuy() : symbol.getCountRangeForSell();
        final int totalQuantity = (isBuy ? symbol.getMinimumCountForBuy() : symbol.getMinimumCountForSell()) +
                random.nextInt(quantityRange);

        //decide subscriber
        final Subscriber subscriber = randomSubscriber();

        //decide price
        final int price = PriceGenerator.randomPrice(stockId, symbol.getMinimumPriceForBuy(), symbol.getMaximumPriceForBuy(),
                symbol.getMinimumPriceForSell(), symbol.getMaximumPriceForSell(), isBuy, this.matchPercent);

        return new Order(totalQuantity, (byte) subscriber.getId(), price, subscriber.getPriority(), orderSide);
    }

    private void putToQueue(Order order, OrderSide orderSide, int stockId) throws NMSException {
        engine.putOrder(order, orderSide, stockId, symbols.get(stockId).getTradePrice());
    }

    private Subscriber randomSubscriber() {
        return customers.get(new Random().nextInt(customers.size()));
    }

    private Symbol getSymbolWithId(int stockId) {
        return this.symbols.get(stockId);
    }

    @Override
    public void setParameters(int preOpeningTime, int tradingTime, int buyOrdersCount, int sellOrdersCount,
                              int preOpeningBuyOrdersCount, int preOpeningSellOrdersCount, int matchPercent,
                              Set<Symbol> symbols, Set<Subscriber> customers) {
        this.preOpeningTime = preOpeningTime;
        this.tradingTime = tradingTime;
        this.buyOrdersCount = buyOrdersCount - preOpeningBuyOrdersCount;
        this.sellOrdersCount = sellOrdersCount - preOpeningSellOrdersCount;
        this.preOpeningBuyOrdersCount = preOpeningBuyOrdersCount;
        this.preOpeningSellOrdersCount = preOpeningSellOrdersCount;
        this.matchPercent = matchPercent;

        this.symbols = new HashMap<>();
        this.stockIds = new ArrayList<>(symbols.size());
        for (Symbol s : symbols) {
            this.symbols.put(s.getStockId(), s);
            this.stockIds.add(s.getStockId());
        }
        this.stocksCount = stockIds.size();

        this.customers = new ArrayList<>(customers.size());
        for (Subscriber s : customers) {
            this.customers.add(s);
        }
        settings.setStatus(Settings.EngineStatus.SETTINGS_COMPLETE);
        working = true;
    }


    @Override
    public void startProcess() throws NMSException {
        processStarter = new Thread(){
            public void run(){
                working = true;
                try {
                    activityLogger.init("aclog");
                } catch (IOException e) {
                    log.warn("exception on activity log file", e);
                    //throw new NMSException(NMSException.ErrorCode.INTERNAL_SERVER_ERROR, "exception on activity log file: "+e.getMessage());
                }
                log.debug("Starting pre-opening phase");
                if(!working) return;
                engine.startPreOpening();
                if(!working) return;
                preOpeningGeneration();
                log.info("finished pre-opening generation");
                if(!working) return;
                engine.startTrading();
                final Thread buyOrderGenerator = new Thread(new CountBasedOrderGenerator(OrderSide.BUY, tradingTime));
                final Thread sellOrderGenerator = new Thread(new CountBasedOrderGenerator(OrderSide.SELL, tradingTime));
                if(!working) return;
                buyOrderGenerator.start();
                sellOrderGenerator.start();
                try {
                    Thread.sleep(tradingTime * 60 * 1000);
                } catch (InterruptedException e) {
                    log.warn("main process thread interrupted: " + e.getMessage());
                }
                if(!working) return;
                log.info("process finished");
                try {
                    engine.stop();
                } catch (NMSException e) {
                    log.warn("exception on engine stop ", e);
                }

            }
        };
        processStarter.start();
    }

    private void preOpeningGeneration() {
        final long defaultWaitTimeNano = ((((long)this.preOpeningTime) * 60 * 1000 * 1000000 / (preOpeningBuyOrdersCount + preOpeningSellOrdersCount)));

        //pre-opening sell orders
        new Thread(new Runnable() {
            @Override
            public void run() {
                generate(defaultWaitTimeNano, preOpeningSellOrdersCount, OrderSide.SELL);
            }
        }).start();

        //pre-opening buy orders
        new Thread(new Runnable() {
            @Override
            public void run() {
                generate(defaultWaitTimeNano, preOpeningBuyOrdersCount, OrderSide.BUY);
            }
        }).start();

        try {
            Thread.sleep(preOpeningTime * 60 * 1000);
        } catch (InterruptedException e) {
            log.warn("main process thread interrupted: " + e.getMessage());
        }
    }

    private void generate(long defaultLatencyNano, int totalOrders, OrderSide orderside) {
        final Random random = new Random();
        for (int counter = 0; counter < totalOrders; counter++) {
            while (settings.getStatus().equals(Settings.EngineStatus.PAUSED)){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    log.warn("exception on generator waiting while !working", e);
                }
            }
            if(!settings.getStatus().equals(Settings.EngineStatus.PRE_OPENING) &&
                    !settings.getStatus().equals(Settings.EngineStatus.TRADING)){
                break;
            }

            final long startTime = System.nanoTime();
            try {
                final int stockId = stockIds.get(random.nextInt(stocksCount));
                final Order order = randomOrder(orderside, stockId);
                putToQueue(order, orderside, stockId);

            } catch (NMSException e) {
                log.warn("Exception on putToQueue: " + e.getMessage());
            }
            final long latency = defaultLatencyNano - (System.nanoTime() - startTime);
            if (latency > 0) {
                //can't wait for nano or micro seconds, and the latency is smaller than milliseconds
            }
        }
    }

    @Override
    public void togglePauseProcess() throws NMSException {
        if(this.working){
            this.working = false;
            engine.pause();
        }else {
            this.working = true;
            engine.resume();
        }
    }

    @Override
    public void restartProcess() throws NMSException {
        log.info("restarting process...");
        engine.restart();
        //stopProcess();
        //startProcess();

    }

    @Override
    public void stopProcess() throws NMSException {
        this.working = false;
        log.info("stop invoked, interrupting main process thread");
        try{
            this.processStarter.interrupt();
        }catch (Throwable e){
            log.info("main process thread interrupted exception: " + e.getMessage());
        }
        engine.stop();
    }

    class CountBasedOrderGenerator implements Runnable {
        private final long tradingDuration;
        private final OrderSide orderSide;
        private final boolean isBuy;
        private final int ordersCount;

        CountBasedOrderGenerator(OrderSide orderSide, int tradingTimeMins) {
            this.tradingDuration = tradingTimeMins * 60 * 1000;
            this.orderSide = orderSide;
            this.isBuy = orderSide.equals(OrderSide.BUY);
            this.ordersCount = isBuy ? buyOrdersCount : sellOrdersCount;
        }

        @Override
        public void run() {
            final long defaultLatency = tradingDuration / ordersCount;
            generate(defaultLatency, ordersCount, orderSide);
            log.info("Finished generating orders for side: " + orderSide + ", time: " + System.nanoTime());
        }
    }
}
