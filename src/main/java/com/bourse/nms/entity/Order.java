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

//    private final OrderType type;     not needed in this phase
//    private final OrderValidity validity;     not needed in this phase
//    private final int orderQuantity;      not needed in this phase, we'll only use totalQuantity
    private final int stockId;
    private final int totalQuantity;
    private final int subscriberId; //todo: have to be long for more serious implementations
    private final OrderSide orderSide;
    private final long price;
    private final int subscriberPriority;   //order account type code
    private final long creationTime;

    public Order(int stockId, int totalQuantity, int subscriberId, OrderSide orderSide, long price, int subscriberPriority) {
        this.stockId = stockId;
        this.totalQuantity = totalQuantity;
        this.subscriberId = subscriberId;
        this.orderSide = orderSide;
        this.price = price;
        this.subscriberPriority = subscriberPriority;
        this.creationTime = System.nanoTime();
    }

    public int getStockId() {
        return stockId;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public int getSubscriberId() {
        return subscriberId;
    }

    public OrderSide getOrderSide() {
        return orderSide;
    }

    public long getPrice() {
        return price;
    }

    public int getSubscriberPriority() {
        return subscriberPriority;
    }

    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public int compareTo(Object o) {
        final Order order = (Order) o;
        final long priceDif = price - order.getPrice();
        if (priceDif == 0) {
            final int priorityDif = subscriberPriority - order.getSubscriberPriority();
            if (priorityDif == 0) {
                return (int) (order.getCreationTime() - creationTime);
            } else {
                return priorityDif;
            }
        } if(order.getOrderSide().equals(OrderSide.BUY)) return -1 * (int) priceDif;
        else return (int) priceDif;
    }


}
