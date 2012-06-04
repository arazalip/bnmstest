package com.bourse.nms.generator;

import com.bourse.nms.common.NMSException;
import com.bourse.nms.entity.Subscriber;
import com.bourse.nms.entity.Symbol;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: araz
 * Date: 6/1/12
 * Time: 2:02 PM
 */
public interface Generator {

    public void setParameters(int preOpeningTime, int tradingTime, int buyOrdersCount, int sellOrdersCount,
                              int preOpeningBuyOrdersCount, int preOpeningSellOrdersCount, int matchPercent,
                              Set<Symbol> symbols, Set<Subscriber> customers) throws NMSException;

    public void startProcess() throws NMSException;

    public void pauseProcess() throws NMSException;

    public void restartProcess() throws NMSException;

    public void stopProcess() throws NMSException;
}
