package com.bourse.nms.generator;

import com.bourse.nms.common.NMSException;
import com.bourse.nms.engine.Engine;
import com.bourse.nms.entity.Order;
import com.bourse.nms.entity.Order.OrderSide;
import com.bourse.nms.entity.Subscriber;
import com.bourse.nms.entity.Symbol;
import com.bourse.nms.log.ActivityLogger;
import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: amin
 * Date: 6/1/12
 * Time: 12:48 PM
 */
public class GeneratorImpl implements Generator {

    private static Logger log = Logger.getLogger(GeneratorImpl.class);
    private static ActivityLogger activityLogger = new ActivityLogger();

    @Autowired
    private Engine engine;
    private boolean working;

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

    public GeneratorImpl() {

    }

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
        final int price = PriceGenerator.randomPrice(symbol.getMinimumPriceForBuy(), symbol.getMaximumPriceForBuy(),
                symbol.getMinimumPriceForSell(), symbol.getMaximumPriceForSell(), isBuy, this.matchPercent);

        return new Order(totalQuantity, (byte) subscriber.getId(), price, subscriber.getPriority());
    }

    private void putToQueue(Order order, OrderSide orderSide, int stockId) throws NMSException {
        engine.putOrder(order, orderSide, stockId);
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

        working = true;
    }

    @Override
    public void startProcess() {
        log.debug("Starting pre-opening phase");
        engine.startPreOpening();
        preopeningGeneration();
        log.info("finished pre-opening generation");

        log.info("starting trading session");
        engine.startTrading();
        final Thread buyOrderGenerator = new Thread(new CountBasedOrderGenerator(OrderSide.BUY, tradingTime));
        final Thread sellOrderGenerator = new Thread(new CountBasedOrderGenerator(OrderSide.SELL, tradingTime));
        buyOrderGenerator.start();
        sellOrderGenerator.start();
        try {
            Thread.sleep(tradingTime * 60 * 1000);
        } catch (InterruptedException e) {
            log.warn("exception on trading wait", e);
        }
        log.info("process finished");
        engine.stop();
    }

    private void preopeningGeneration() {
        final long defaultWaitTime = (this.preOpeningTime * 60 * 1000 / (preOpeningBuyOrdersCount + preOpeningSellOrdersCount)) * 2;

        //pre-opening sell orders
        new Thread(new Runnable() {
            @Override
            public void run() {
                generate(defaultWaitTime, preOpeningSellOrdersCount, OrderSide.SELL);
            }
        }).start();

        //pre-opening buy orders
        new Thread(new Runnable() {
            @Override
            public void run() {
                generate(defaultWaitTime, preOpeningBuyOrdersCount, OrderSide.BUY);
            }
        }).start();

        try {
            Thread.sleep(preOpeningTime * 60 * 1000);
        } catch (InterruptedException e) {
            log.warn("exception on waiting for pre-opening time", e);
        }
    }

    private void generate(long defaultLatency, int totalOrders, OrderSide orderside) {
        final Random random = new Random();
        for (int counter = 0; working && counter < totalOrders; counter++) {
            final long startTime = System.currentTimeMillis();
            try {
                final int stockId = stockIds.get(random.nextInt(stocksCount));
                final Order order = randomOrder(orderside, stockId);
                putToQueue(order, orderside, stockId);

            } catch (NMSException e) {
                log.warn("Exception on putToQueue", e);
            }
            final long latency = defaultLatency - (System.currentTimeMillis() - startTime);
            if (latency > 0) {
                try {
                    Thread.sleep(latency);
                } catch (InterruptedException e) {
                    log.warn("Couldn't wait properly", e);
                }
            }
        }
    }

    @Override
    public void pauseProcess() {
        //todo: this won't work...
        this.working = false;
        engine.pause();
    }

    @Override
    public void restartProcess() {
        log.info("restarting process...");
        stopProcess();
        startProcess();
    }

    @Override
    public void stopProcess() {
        this.working = false;
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

    public static void main(String[] args) {

        final int preopeningTime = 0;
        final int tradingTime = 0;
        final int buyOrdersCount = 10000000;
        final int sellOrdersCount = 10000000;
        final int preopeningBuyOrdersCount = 0;
        final int preopeningSellOrdersCount = 0;
        final int matchPercent = 0;
        final Set<Symbol> symbols = new HashSet<>();
        symbols.add(new Symbol(1, "1", "1", 100, 1000, 200, 2000, 10, 100, 10, 100, 500));
        symbols.add(new Symbol(2, "2", "2", 200, 2000, 200, 2000, 20, 200, 20, 200, 500));
        symbols.add(new Symbol(3, "3", "3", 300, 3000, 300, 3000, 30, 300, 30, 300, 500));

        final Set<Subscriber> customers = new HashSet<>();
        customers.add(new Subscriber(1, 1, 1));
        customers.add(new Subscriber(2, 2, 2));
        customers.add(new Subscriber(3, 3, 3));

        Generator generator = new GeneratorImpl();
        //testing this will throw NullPointerException right now...
        try {
            generator.setParameters(preopeningTime, tradingTime, buyOrdersCount, sellOrdersCount,
                    preopeningBuyOrdersCount, preopeningSellOrdersCount, matchPercent, symbols, customers);
            log.info("starting test @ " + System.nanoTime());
            generator.startProcess();
        } catch (NMSException e) {
            e.printStackTrace();
        }
    }
}
