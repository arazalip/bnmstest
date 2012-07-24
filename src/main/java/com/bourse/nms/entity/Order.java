package com.bourse.nms.entity;

/**
 * order class
 */
public class Order implements Comparable {

    private static final long MAX_PRICE_VALUE = 0x7FFFFl;

    public static enum OrderSide {
        BUY,
        SELL
    }

    /**
     * order quantity
     */
    private final int totalQuantity;
    /**
     * subscriber id
     */
    private final byte subscriberId;
    /**
     * code that is generated for order
     */
    private final long orderCode;

    private final boolean isBuy;

    /**
     * Order constructor
     * @param totalQuantity quantity
     * @param subscriberId subscriber id
     * @param price price
     * @param subscriberPriority subscriber priority
     */
    public Order(int totalQuantity, byte subscriberId, long price, int subscriberPriority, OrderSide orderSide) {
        this.totalQuantity = totalQuantity;
        this.subscriberId = subscriberId;
        this.isBuy = orderSide.equals(OrderSide.BUY);
        final long timeFactor = Long.MAX_VALUE - System.nanoTime();
        this.orderCode = buildOrderCode(price, subscriberPriority, timeFactor, orderSide);
    }

    /**
     * returns order quantity
     * @return quantity
     */
    public int getTotalQuantity() {
        return totalQuantity;
    }

    /**
     * returns subscriber id
     * @return subscriber id
     */
    public byte getSubscriberId() {
        return subscriberId;
    }

    /**
     * returns order price
     * @return price
     */
    public long getPrice() {
        if(isBuy)
            return ((orderCode & 0xFFFFF00000000000l) >> 44);
        else
            return MAX_PRICE_VALUE - ((orderCode & 0xFFFFF00000000000l) >> 44);
    }

    /**
     * returns orders subscriber priority
     * @return subscriber priority
     */
    public byte getSubscriberPriority() {
        return (byte) ((orderCode & 0x00000F00000000000l) >> 40);
    }

    /**
     * returns order code
     * @return order code
     */
    public long getOrderCode() {
        return this.orderCode;
    }

    /**
     * comparator method. compares order codes
     * @param o object to compare to
     * @return Long.compare(this.getOrderCode, o.getOrderCode)
     */
    @Override
    public int compareTo(Object o) {
        //final Order order = (Order) o;
        return Long.compare(this.getOrderCode(), ((Order) o).getOrderCode());
    }

    /**
     * creates order code by arguments
     * @param price order price
     * @param priority order subscribers priority
     * @param time order put time
     * @return order code
     */
    public static long buildOrderCode(long price, int priority, long time, OrderSide orderSide) {
        if(orderSide.equals(OrderSide.BUY))
            return (price & MAX_PRICE_VALUE) << 44 | ((priority & 0x0Fl) << 40) | ((time & 0xFFFFFFFFFF000l) >> 12);
        else
            return ((MAX_PRICE_VALUE - price) & MAX_PRICE_VALUE) << 44 | ((priority & 0x0Fl) << 40) | ((time & 0xFFFFFFFFFF000l) >> 12);
    }

    /**
     * toString overridden for activity log
     * @return string representation of order for activity log
     */
    @Override
    public String toString() {
        return orderCode + "," + subscriberId + "," + totalQuantity + "," + getPrice();
    }
}
