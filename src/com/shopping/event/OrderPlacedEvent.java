package com.shopping.event;

import com.shopping.order.Order;

public class OrderPlacedEvent {
    private final Order order;

    public OrderPlacedEvent(Order order) {
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }
}
