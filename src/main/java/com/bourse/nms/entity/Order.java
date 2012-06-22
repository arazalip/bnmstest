package com.bourse.nms.entity;

/**
 * Created with IntelliJ IDEA.
 * User: amin
 * Date: 6/1/12
 * Time: 12:48 PM
 */
public class Order implements Comparable {

    public static enum OrderSide {
        BUY,
        SELL
    }

    private final int totalQuantity;
    private final byte subscriberId;
    private final long orderCode;

    public Order(int totalQuantity, byte subscriberId, long price, int subscriberPriority) {
        this.totalQuantity = totalQuantity;
        this.subscriberId = subscriberId;
        final long timeFactor = Long.MAX_VALUE - System.nanoTime();
        this.orderCode = buildOrderCode(price, subscriberPriority, timeFactor);
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public byte getSubscriberId() {
        return subscriberId;
    }

    public long getPrice() {
        return ((orderCode & 0xFFFFF00000000000l) >> 44);
    }

    public byte getSubscriberPriority() {
        return (byte) ((orderCode & 0x00000F00000000000l) >> 40);
    }

    public long getOrderCode() {
        return this.orderCode;
    }

    @Override
    public int compareTo(Object o) {
        final Order order = (Order) o;
        //todo: find a faster way... note that casting from long to int can generate pure nonsense
        final long dif = (this.orderCode - order.getOrderCode());
        return dif == 0 ? 0 : (int) (dif / Math.abs(dif));
    }

    public static long buildOrderCode(long price, int priority, long time) {
        return (price & 0x7FFFFl) << 44 | ((priority & 0x0Fl) << 40) | ((time & 0xFFFFFFFFFF000l) >> 12);
    }

    @Override
    public String toString() {
        return totalQuantity + "," + subscriberId + "," + orderCode;
    }

    public static void main(String[] args) {
        final Order order1 = new Order(10, (byte) 10, 10000, 10);
        final Order order2 = new Order(10, (byte) 10, 10000, 10);
        System.out.println(order1.compareTo(order2));
    }
}
