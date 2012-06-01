package com.bourse.nms.entity;

/**
 * Created by IntelliJ IDEA.
 * User: araz
 * Date: 6/1/12
 * Time: 2:07 PM
 */
public class Symbol {

    private final String stockId;
    private final String name;
    private final String abbrv;
    private final int minimumPriceForBuy;
    private final int maximumPriceForBuy;
    private final int minimumPriceForSell;
    private final int maximumPriceForSell;
    private final int minimumCountForBuy;
    private final int maximumCountForBuy;
    private final int minimumCountForSell;
    private final int maximumCountForSell;
    private final int tradePrice;

    public Symbol(String stockId, String name, String abbrv, int minimumPriceForBuy, int maximumPriceForBuy,
                  int minimumPriceForSell, int maximumPriceForSell, int minimumCountForBuy, int maximumCountForBuy,
                  int minimumCountForSell, int maximumCountForSell, int tradePrice) {
        this.stockId = stockId;
        this.name = name;
        this.abbrv = abbrv;
        this.minimumPriceForBuy = minimumPriceForBuy;
        this.maximumPriceForBuy = maximumPriceForBuy;
        this.minimumPriceForSell = minimumPriceForSell;
        this.maximumPriceForSell = maximumPriceForSell;
        this.minimumCountForBuy = minimumCountForBuy;
        this.maximumCountForBuy = maximumCountForBuy;
        this.minimumCountForSell = minimumCountForSell;
        this.maximumCountForSell = maximumCountForSell;
        this.tradePrice = tradePrice;
    }

    public String getStockId() {
        return stockId;
    }

    public String getName() {
        return name;
    }

    public String getAbbrv() {
        return abbrv;
    }

    public int getMinimumPriceForBuy() {
        return minimumPriceForBuy;
    }

    public int getMaximumPriceForBuy() {
        return maximumPriceForBuy;
    }

    public int getMinimumPriceForSell() {
        return minimumPriceForSell;
    }

    public int getMaximumPriceForSell() {
        return maximumPriceForSell;
    }

    public int getMinimumCountForBuy() {
        return minimumCountForBuy;
    }

    public int getMaximumCountForBuy() {
        return maximumCountForBuy;
    }

    public int getMinimumCountForSell() {
        return minimumCountForSell;
    }

    public int getMaximumCountForSell() {
        return maximumCountForSell;
    }

    public int getTradePrice() {
        return tradePrice;
    }
}
