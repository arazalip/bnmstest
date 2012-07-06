package com.bourse.nms.generator;

import com.bourse.nms.common.NMSException;
import com.bourse.nms.entity.Subscriber;
import com.bourse.nms.entity.Symbol;

import java.util.Set;

/**
 * Generator interface
 */
public interface Generator {
    /**
     * sets software settings and params
     * @param preOpeningTime pre-opening time in minutes
     * @param tradingTime trading time in minutes
     * @param buyOrdersCount total buy orders count
     * @param sellOrdersCount total sell orders count
     * @param preOpeningBuyOrdersCount pre-opening buy orders count
     * @param preOpeningSellOrdersCount pre-opening sell orders count
     * @param matchPercent match percent(how many trades can be done by orders)
     * @param symbols stock symbols
     * @param customers subscribers
     * @throws NMSException
     */
    public void setParameters(int preOpeningTime, int tradingTime, int buyOrdersCount, int sellOrdersCount,
                              int preOpeningBuyOrdersCount, int preOpeningSellOrdersCount, int matchPercent,
                              Set<Symbol> symbols, Set<Subscriber> customers) throws NMSException;

    /**
     * starts pre-opening and then trading
     * @throws NMSException
     */
    public void startProcess() throws NMSException;

    /**
     * pauses process
     * @throws NMSException
     */
    public void togglePauseProcess() throws NMSException;

    /**
     * restarts process
     * @throws NMSException
     */
    public void restartProcess() throws NMSException;

    /**
     * stops process
     * @throws NMSException
     */
    public void stopProcess() throws NMSException;
}
