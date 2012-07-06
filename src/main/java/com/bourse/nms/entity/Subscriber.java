package com.bourse.nms.entity;

/**
 * subscriber entity
 */
public class Subscriber {

    private final int id;
    private final int brokerId;
    private final int priority;

    public Subscriber(int id, int brokerId, int priority) {
        this.id = id;
        this.brokerId = brokerId;
        this.priority = priority;
    }

    public int getId() {
        return id;
    }

    public int getBrokerId() {
        return brokerId;
    }

    public int getPriority() {
        return priority;
    }
}
