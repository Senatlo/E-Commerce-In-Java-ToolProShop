package com.shopping.order;

import com.shopping.cart.ShoppingCart;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents an finalized order snapshot.
 */
public class Order {
    private final String orderId;
    private final String customerName;
    private final ShoppingCart cart;
    private final LocalDateTime timestamp;
    private final double finalTotal;

    public Order(ShoppingCart cart, String customerName) {
        this.orderId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.customerName = customerName;
        this.cart = cart;
        // The final total is locked in at the time of order creation.
        this.finalTotal = cart.calculateTotal();
    }

    public String getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public ShoppingCart getCart() { return cart; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getFinalTotal() { return finalTotal; }
}
