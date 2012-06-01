package com.bourse.nms.generator;

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
                              int preOpeningOrdersCount, int preOpeningOrdersPercent, Set<Symbol> symbols, Set<Subscriber> customers);

    public void startProcess();

    public void pauseProcess();

    public void restartProcess();

    public void stopProcess();
}
