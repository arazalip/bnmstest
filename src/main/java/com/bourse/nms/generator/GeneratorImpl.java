package com.bourse.nms.generator;

import com.bourse.nms.common.NMSException;
import com.bourse.nms.entity.Subscriber;
import com.bourse.nms.entity.Symbol;
import org.apache.log4j.Logger;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: araz
 * Date: 6/1/12
 * Time: 2:42 PM
 */
public class GeneratorImpl implements Generator {

    private final static Logger log = Logger.getLogger(GeneratorImpl.class);

    @Override
    public void setParameters(int preOpeningTime, int tradingTime, int buyOrdersCount, int sellOrdersCount, int preOpeningOrdersCount, int matchPercent, Set<Symbol> symbols, Set<Subscriber> customers)  throws NMSException {
        log.debug("got data: ");
    }

    @Override
    public void startProcess() {

    }

    @Override
    public void pauseProcess()  throws NMSException{

    }

    @Override
    public void restartProcess()  throws NMSException{

    }

    @Override
    public void stopProcess()  throws NMSException{

    }
}
