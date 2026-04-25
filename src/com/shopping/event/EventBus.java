package com.shopping.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lightweight static Event Bus for broadcasting notifications
 * across the entire application without tight coupling.
 */
public class EventBus {
    
    private static final List<InventoryAlertListener> listeners = new CopyOnWriteArrayList<>();

    public static void registerListener(InventoryAlertListener listener) {
        listeners.add(listener);
    }
    
    public static void unregisterListener(InventoryAlertListener listener) {
        listeners.remove(listener);
    }

    public static void publish(OrderPlacedEvent event) {
        for (InventoryAlertListener listener : listeners) {
            listener.onOrderPlaced(event);
        }
    }
}
