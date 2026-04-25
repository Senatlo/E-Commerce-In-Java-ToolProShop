package com.shopping.order;

import com.shopping.cart.CartItem;
import com.shopping.cart.ShoppingCart;
import com.shopping.dao.ProductDAO;
import com.shopping.exception.OutOfStockException;
import com.shopping.model.Product;
import com.shopping.event.EventBus;
import com.shopping.event.OrderPlacedEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Handles checkout: saves the order to the database (inside a transaction)
 * AND writes a plain-text record to the "orders/" directory on disk.
 *
 * Two-layer persistence satisfies both the DB-query requirements (admin
 * order history, analytics) and the spec's "File storage for orders"
 * technical requirement.
 */
public class OrderProcessor {

    private final ProductDAO productDAO;

    public OrderProcessor(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public Order checkout(ShoppingCart cart, int userId, String customerName) {
        Connection connection = productDAO.getConnection();
        Order order = null;

        try {
            connection.setAutoCommit(false); // BEGIN TRANSACTION

            // 1. Verify and update stock using row-level locks (FOR UPDATE)
            for (CartItem item : cart.getItems()) {
                Product p = productDAO.getProductForUpdate(item.getProduct().getId());

                if (p == null || p.getStockQuantity() < item.getQuantity()) {
                    throw new OutOfStockException(
                        item.getProduct().getId(),
                        item.getQuantity(),
                        p != null ? p.getStockQuantity() : 0
                    );
                }

                int newStock = p.getStockQuantity() - item.getQuantity();
                productDAO.updateStock(p.getId(), newStock);

                // Keep live references updated for the event dispatcher
                item.getProduct().setStockQuantity(newStock);
            }

            // 2. Generate the Order snapshot
            order = new Order(cart, customerName);

            // 3. Insert order header into DB
            String insertOrder = "INSERT INTO Orders (id, user_id, total_amount) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(insertOrder)) {
                stmt.setString(1, order.getOrderId());
                stmt.setInt(2, userId);
                stmt.setDouble(3, order.getFinalTotal());
                stmt.executeUpdate();
            }

            // 4. Insert order line-items in a batch
            String insertItems =
                "INSERT INTO Order_Items (order_id, product_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(insertItems)) {
                for (CartItem item : cart.getItems()) {
                    stmt.setString(1, order.getOrderId());
                    stmt.setString(2, item.getProduct().getId());
                    stmt.setInt(3, item.getQuantity());
                    stmt.setDouble(4, item.getProduct().getPrice());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            // 5. Commit DB transaction
            connection.commit();

            // 6. ?????? FILE STORAGE ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            //    Write a plain-text receipt to orders/<orderId>.txt on disk.
            //    This runs AFTER a successful commit so the file is only
            //    created when the order is definitively placed.
            OrderFileStorage.save(order);

        } catch (OutOfStockException | SQLException e) {
            try {
                if (connection != null) connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            throw new RuntimeException("Checkout failed! Rolled back gracefully: " + e.getMessage(), e);
        } finally {
            try {
                if (connection != null) connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Broadcast the event after all persistence succeeds
        if (order != null) {
            EventBus.publish(new OrderPlacedEvent(order));
        }

        return order;
    }
}
