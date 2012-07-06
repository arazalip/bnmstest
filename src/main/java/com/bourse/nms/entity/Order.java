package com.bourse.nms.entity;

/**
 * order class
 */
public class Order implements Comparable {

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

    /**
     * Order constructor
     * @param totalQuantity quantity
     * @param subscriberId subscriber id
     * @param price price
     * @param subscriberPriority subscriber priority
     */
    public Order(int totalQuantity, byte subscriberId, long price, int subscriberPriority) {
        this.totalQuantity = totalQuantity;
        this.subscriberId = subscriberId;
        final long timeFactor = Long.MAX_VALUE - System.nanoTime();
        this.orderCode = buildOrderCode(price, subscriberPriority, timeFactor);
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
        return ((orderCode & 0xFFFFF00000000000l) >> 44);
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
    public static long buildOrderCode(long price, int priority, long time) {
        return (price & 0x7FFFFl) << 44 | ((priority & 0x0Fl) << 40) | ((time & 0xFFFFFFFFFF000l) >> 12);
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
