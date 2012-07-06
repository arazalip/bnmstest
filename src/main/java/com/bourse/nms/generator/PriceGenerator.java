package com.bourse.nms.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * price generator based on stock properties
 */
public class PriceGenerator {
    private final static Map<Integer, Random> stockRandomGenerators = new HashMap<>();
    /**
     * generates random price by price intervals and match percent
     *
     * @param minimumPriceForBuy minimumPriceForBuy
     * @param maximumPriceForBuy maximumPriceForBuy
     * @param minimumPriceForSell minimumPriceForSell
     * @param maximumPriceForSell maximumPriceForSell
     * @param isBuy               order buy or sell
     * @param matchPercent        match percent
     * @return generates order price by match percent
     */
    public static int randomPrice(int stockId,
                                  int minimumPriceForBuy, int maximumPriceForBuy,
                                  int minimumPriceForSell, int maximumPriceForSell,
                                  boolean isBuy, int matchPercent) {
        final int minInterval = Math.max(minimumPriceForBuy, minimumPriceForSell);
        final int maxInterval = Math.min(maximumPriceForBuy, maximumPriceForSell);
        final int mean = (maxInterval - minInterval) / 2 + minInterval;

        if(!stockRandomGenerators.containsKey(stockId)){
            stockRandomGenerators.put(stockId, new Random());
        }
        final Random r = stockRandomGenerators.get(stockId);
        final int which = r.nextInt(100);
        if (which < matchPercent) {
            if (isBuy) return r.nextInt(maximumPriceForBuy - mean) + mean;
            else return r.nextInt(mean - minimumPriceForSell) + minimumPriceForSell;
        } else {
            if (isBuy) return r.nextInt(mean - minimumPriceForBuy) + minimumPriceForBuy;
            else return r.nextInt(maximumPriceForSell - mean) + mean;
        }

    }

}
