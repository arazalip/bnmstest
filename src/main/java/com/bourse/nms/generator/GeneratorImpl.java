package com.bourse.nms.generator;

import com.bourse.nms.common.NMSException;
import com.bourse.nms.engine.Engine;
import com.bourse.nms.entity.Order;
import com.bourse.nms.entity.Subscriber;
import com.bourse.nms.entity.Symbol;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: amin
 * Date: 6/1/12
 * Time: 12:48 PM
 */
public class GeneratorImpl implements Generator {
    private static Logger log = Logger.getLogger(GeneratorImpl.class);
    private final Engine engine;
    private boolean working;

    private final int preOpeningTime;   //in minutes
    private final int tradingTime;  //in minutes
    private final int buyOrdersCount;
    private final int sellOrdersCount;
    private final int preOpeningBuyOrdersCount;
    private final int preOpeningSellOrdersCount;
    private final int matchPercent; //todo: how shall we deal with this?
    private final ArrayList<Subscriber> customers;
    private final Map<Integer, Symbol> symbols;
    private final ArrayList<Integer> stockIds;
    private final int stocksCount;

    public GeneratorImpl(int preOpeningTime, int tradingTime, int buyOrdersCount, int sellOrdersCount,
                         int preOpeningBuyOrdersCount, int preOpeningSellOrdersCount, int matchPercent,
                         Set<Symbol> symbols, Set<Subscriber> customers, Engine engine) {
        this.preOpeningTime = preOpeningTime;
        this.tradingTime = tradingTime;
        this.buyOrdersCount = buyOrdersCount;
        this.sellOrdersCount = sellOrdersCount;
        this.preOpeningBuyOrdersCount = preOpeningBuyOrdersCount;
        this.preOpeningSellOrdersCount = preOpeningSellOrdersCount;
        this.matchPercent = matchPercent;
        this.engine = engine;

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

    public Order randomOrder(Order.OrderSide orderSide) {
        final Random random = new Random();
        final boolean isBuy = orderSide.equals(Order.OrderSide.BUY);

        //decide symbol
        final Symbol symbol = getSymbolWithId(stockIds.get(random.nextInt(stocksCount)));

        //decide quantity
        final int quantityRange = isBuy ? symbol.getCountRangeForBuy() : symbol.getCountRangeForSell();
        final int totalQuantity = (isBuy ? symbol.getMinimumCountForBuy() : symbol.getMinimumCountForSell()) +
                random.nextInt(quantityRange);

        //decide subscriber
        final Subscriber subscriber = randomSubscriber();

        //decide price
        final int priceRange = isBuy ? symbol.getPriceRangeForBuy() : symbol.getPriceRangeForSell();
        final int price = (isBuy ? symbol.getMinimumPriceForBuy() : symbol.getMinimumPriceForSell()) +
                random.nextInt(priceRange);

        return new Order(symbol.getStockId(), totalQuantity, subscriber.getId(), orderSide, price, subscriber.getPriority());
    }

    private void putToQueue(Order order) {
        engine.putOrder(order);
    }

    private Subscriber randomSubscriber() {
        return customers.get(new Random().nextInt(customers.size()));
    }

    private Symbol getSymbolWithId(int stockId) {
        return this.symbols.get(stockId);
    }

    @Override
    public void setParameters(int preOpeningTime, int tradingTime, int buyOrdersCount, int sellOrdersCount,
                              int preOpeningOrdersCount, int matchPercent, Set<Symbol> symbols,
                              Set<Subscriber> customers) {

    }

    @Override
    public void startProcess() {
        log.info("Starting pre-opening phase");
        engine.startPreOpening();
        preopeningGeneration();
        log.info("finished pre-opening generation");

        log.info("starting trading session");
        engine.startTrading();
        final Thread buyOrderGenerator = new Thread(new CountBasedOrderGenerator(Order.OrderSide.BUY, tradingTime));
        final Thread sellOrderGenerator = new Thread(new CountBasedOrderGenerator(Order.OrderSide.SELL, tradingTime));
        buyOrderGenerator.start();
        sellOrderGenerator.start();
    }

    private void preopeningGeneration() {
        final long defaultWaitTime = this.preOpeningTime * 60 * 1000 / (preOpeningBuyOrdersCount + preOpeningSellOrdersCount);

        //pre-opening sell orders
        generate(defaultWaitTime, preOpeningSellOrdersCount, Order.OrderSide.SELL);

        //pre-opening buy orders
        generate(defaultWaitTime, preOpeningBuyOrdersCount, Order.OrderSide.BUY);
    }

    private void generate(long defaultLatency, int totalOrders, Order.OrderSide orderside) {
        for (int counter = 0; working && counter < totalOrders; counter++) {
            final long startTime = System.currentTimeMillis();
            putToQueue(randomOrder(orderside));
            final long latency = defaultLatency - (System.currentTimeMillis() - startTime);
            if (latency > 0)
                try {
                    Thread.sleep(latency);
                } catch (InterruptedException e) {
                    log.warn("Couldn't wait properly");
                }
        }
    }

    @Override
    public void pauseProcess() {
        //todo: this won't work...
        this.working = false;
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
    }

    class CountBasedOrderGenerator implements Runnable {
        private final long tradingDuration;
        private final Order.OrderSide orderSide;
        private final boolean isBuy;
        private final int ordersCount;

        CountBasedOrderGenerator(Order.OrderSide orderSide, int tradingTimeMins) {
            this.tradingDuration = tradingTimeMins * 60 * 1000;
            this.orderSide = orderSide;
            this.isBuy = orderSide.equals(Order.OrderSide.BUY);
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

        final Engine engine = null;
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

        Generator generator = new GeneratorImpl(preopeningTime, tradingTime, buyOrdersCount, sellOrdersCount,
                preopeningBuyOrdersCount, preopeningSellOrdersCount, matchPercent, symbols, customers, engine);
        log.info("starting test @ " + System.nanoTime());
        //testing this will throw NullPointerException right now...
        try {
            generator.startProcess();
        } catch (NMSException e) {
            e.printStackTrace();
        }
    }
}
