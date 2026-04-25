package com.shopping.event;

public interface InventoryAlertListener {
    void onOrderPlaced(OrderPlacedEvent event);
}
