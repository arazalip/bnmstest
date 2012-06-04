package com.bourse.nms.generator;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: araz
 * Date: 6/4/12
 * Time: 1:38 AM
 */
public class PriceGenerator {

    /**
     * generates random price by price intervals and match percent
     *
     * @param minimumPriceForBuy
     * @param maximumPriceForBuy
     * @param minimumPriceForSell
     * @param maximumPriceForSell
     * @param isBuy               order buy or sell
     * @param matchPercent        match percent
     * @return generates order price by match percent
     */
    public static int randomPrice(int minimumPriceForBuy, int maximumPriceForBuy, int minimumPriceForSell, int maximumPriceForSell,
                                  boolean isBuy, int matchPercent) {
        int minInterval = Math.max(minimumPriceForBuy, minimumPriceForSell);
        int maxInterval = Math.min(maximumPriceForBuy, maximumPriceForSell);
        int mean = maxInterval - minInterval / 2;
        Random r = new Random();

        int which = r.nextInt(100);
        if (which < matchPercent) {
            if (isBuy) return r.nextInt(maximumPriceForBuy - mean) + mean;
            else return r.nextInt(mean - minimumPriceForSell) + minimumPriceForSell;
        } else {
            if (isBuy) return r.nextInt(mean - minimumPriceForBuy) + minimumPriceForBuy;
            else return r.nextInt(maximumPriceForSell - mean) + mean;
        }

    }

}
