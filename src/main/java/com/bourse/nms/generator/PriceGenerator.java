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
     * @param buyInterval buyInterval[0] = minimumPriceForBuy, buyInterval[1] = maximumPriceForBuy
     * @param sellInterval sellInterval[0] = minimumPriceForSell, sellInterval[1] = maximumPriceForSell
     * @param isBuy order buy or sell
     * @param matchPercent match percent
     * @return generates order price by match percent
     */
    public int randomPrice(int[] buyInterval, int[] sellInterval, boolean isBuy, int matchPercent){
        int minInterval = Math.max(buyInterval[0], sellInterval[0]);
        int maxInterval = Math.min(buyInterval[1], sellInterval[1]);
        int mean = maxInterval - minInterval / 2;
        Random r = new Random();

        int which = r.nextInt(100);
        if(which < matchPercent){
            if(isBuy) return r.nextInt(buyInterval[1] - mean) + mean;
            else return r.nextInt(mean - sellInterval[0]) + sellInterval[0];
        }else{
            if(isBuy) return r.nextInt(mean - buyInterval[0]) + buyInterval[0];
            else return r.nextInt(sellInterval[1] - mean) + mean;
        }

    }

}
