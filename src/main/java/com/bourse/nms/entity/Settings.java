package com.bourse.nms.entity;

import java.util.Set;

/**
 * Software Settings. These settings are set from UI
 * User: araz
 * Date: 6/1/12
 * Time: 7:04 PM
 */
public class Settings {

    int preOpeningTime;
    int tradingTime;
    int buyOrdersCount;
    int sellOrdersCount;
    int preOpeningOrdersPercent;
    int matchPercent;
    Set<Symbol> symbols;
    Set<Subscriber> customers;
    volatile EngineStatus status = EngineStatus.INITIALIZING;

    public enum EngineStatus{
        INITIALIZING,
        WAITING,
        SETTINGS_COMPLETE,
        PRE_OPENING,
        TRADING,
        PAUSED,
        FINISHED
    }


    public int getPreOpeningTime() {
        return preOpeningTime;
    }

    public void setPreOpeningTime(int preOpeningTime) {
        this.preOpeningTime = preOpeningTime;
    }

    public int getTradingTime() {
        return tradingTime;
    }

    public void setTradingTime(int tradingTime) {
        this.tradingTime = tradingTime;
    }

    public int getBuyOrdersCount() {
        return buyOrdersCount;
    }

    public void setBuyOrdersCount(int buyOrdersCount) {
        this.buyOrdersCount = buyOrdersCount;
    }

    public int getSellOrdersCount() {
        return sellOrdersCount;
    }

    public void setSellOrdersCount(int sellOrdersCount) {
        this.sellOrdersCount = sellOrdersCount;
    }

    public int getPreOpeningOrdersPercent() {
        return preOpeningOrdersPercent;
    }

    public void setPreOpeningOrdersPercent(int preOpeningOrdersPercent) {
        this.preOpeningOrdersPercent = preOpeningOrdersPercent;
    }

    public int getMatchPercent() {
        return matchPercent;
    }

    public void setMatchPercent(int matchPercent) {
        this.matchPercent = matchPercent;
    }

    public Set<Symbol> getSymbols() {
        return symbols;
    }

    public void setSymbols(Set<Symbol> symbols) {
        this.symbols = symbols;
    }

    public Set<Subscriber> getCustomers() {
        return customers;
    }

    public void setCustomers(Set<Subscriber> customers) {
        this.customers = customers;
    }

    public EngineStatus getStatus() {
        return status;
    }

    public void setStatus(EngineStatus status) {
        this.status = status;
    }
}
