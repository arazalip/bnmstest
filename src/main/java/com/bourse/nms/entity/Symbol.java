package com.bourse.nms.entity;

/**
 * stock symbol entity
 */
public class Symbol {

    private final int stockId;
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
    private final int priceRangeForBuy;
    private final int priceRangeForSell;
    private final int countRangeForBuy;
    private final int countRangeForSell;

    public Symbol(int stockId, String name, String abbrv, int minimumPriceForBuy, int maximumPriceForBuy,
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
        this.priceRangeForBuy = maximumPriceForBuy - minimumPriceForBuy;
        this.priceRangeForSell = maximumPriceForSell - minimumPriceForSell;
        this.countRangeForBuy = maximumCountForBuy - minimumCountForBuy;
        this.countRangeForSell = maximumCountForSell - minimumCountForSell;
    }

    public int getStockId() {
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

    public int getPriceRangeForBuy() {
        return priceRangeForBuy;
    }

    public int getPriceRangeForSell() {
        return priceRangeForSell;
    }

    public int getCountRangeForBuy() {
        return countRangeForBuy;
    }

    public int getCountRangeForSell() {
        return countRangeForSell;
    }
}
