package com.bourse.nms.tmp;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: araz
 * Date: 6/3/12
 * Time: 8:53 PM
 */
public class RandomTest {


    public void generatePrices(int[] buyInterval, int[] sellInterval, int totalCount, int matchPercent){
        int minInterval = Math.max(buyInterval[0], sellInterval[0]);
        int maxInterval = Math.min(buyInterval[1], sellInterval[1]);
        int mean = (maxInterval - minInterval) / 2 + minInterval;
        Random r = new Random();

        int matchCount = 0;
        for(int i = 0; i < totalCount; i++){
            int which = r.nextInt(100);
            int buyPrice;
            int sellPrice;
            if(which < matchPercent){
                buyPrice = r.nextInt(buyInterval[1] - mean) + mean;
                sellPrice = r.nextInt(mean - sellInterval[0]) + sellInterval[0];
            }else{
                buyPrice = r.nextInt(mean - buyInterval[0]) + buyInterval[0];
                sellPrice = r.nextInt(sellInterval[1] - mean) + mean;
            }
            if(buyPrice >= sellPrice)
                matchCount++;
        }

        System.out.println("M: " + matchCount);
    }

    public static void main(String[] args) {
        RandomTest rt = new RandomTest();
        rt.generatePrices(new int[]{100, 3500}, new int[]{500, 2000}, 100000, 30);
    }
}
