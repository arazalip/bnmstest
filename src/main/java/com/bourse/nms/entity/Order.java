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
        return (byte) ((orderCode & 0x00000F0000000000l) >> 40);
    }

    public long getOrderCode() {
        return this.orderCode;
    }

    @Override
    public int compareTo(Object o) {
        final Order order = (Order) o;
        return (int) (this.orderCode - order.getOrderCode());
    }

    private long buildOrderCode(long price, int priority, long time) {
        return (price & 0x7FFFFl) << 44 | ((priority & 0x0Fl) << 40) | (time & 0xFFFFFFFFFFl);
    }

    public static void main(String[] args) {
        final long time = Long.MAX_VALUE - System.nanoTime();
        final byte priority = (byte) 5;
        final int price = 100000;
        System.out.println(time & 0xFFFFFFFFFFl);
        System.out.println(priority & 0x0F);
        System.out.println(price & 0x7FFFF);

        final long orderCode = ((price & 0x7FFFFl) << 44) | ((priority & 0x0Fl) << 40) | (time & 0xFFFFFFFFFFl);
        System.out.println(orderCode);

        final long fetchedPrice = (orderCode & 0xFFFFF00000000000l) >> 44;
        System.out.println(fetchedPrice);

        final byte fetchedPriority = (byte) ((orderCode & 0x00000F0000000000l) >> 40);
        System.out.println(fetchedPriority);

        final long fetchedTime = (orderCode & 0xFFFFFFFFFFl);
        System.out.print(fetchedTime);
    }
}
