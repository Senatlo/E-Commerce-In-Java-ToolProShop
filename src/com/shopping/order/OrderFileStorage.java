package com.shopping.order;

import com.shopping.cart.CartItem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Saves a completed Order to a plain-text file inside the "orders/" directory.
 *
 * File naming convention:
 *   orders/order_<shortId>_<timestamp>.txt
 *
 * This satisfies the "File storage for orders" technical requirement.
 */
public class OrderFileStorage {

    private static final String ORDERS_DIR = "orders";
    private static final DateTimeFormatter DTF =
        DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss", Locale.US);
    private static final DateTimeFormatter DISPLAY_DTF =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US);

    /**
     * Writes the given order to a .txt file.
     * The directory is created automatically if it does not exist.
     *
     * @param order the completed order to persist
     */
    public static void save(Order order) {
        // Ensure the orders directory exists
        File dir = new File(ORDERS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String shortId   = order.getOrderId().substring(0, 8).toUpperCase();
        String timestamp = order.getTimestamp().format(DTF);
        String fileName  = ORDERS_DIR + File.separator
            + "order_" + shortId + "_" + timestamp + ".txt";

        try (BufferedWriter w = new BufferedWriter(new FileWriter(fileName))) {
            w.write("============================================");
            w.newLine();
            w.write("          TOOLSHOP PRO — ORDER RECORD");
            w.newLine();
            w.write("============================================");
            w.newLine();
            w.write(String.format(" Order ID  : %s", order.getOrderId()));
            w.newLine();
            w.write(String.format(" Customer  : %s", order.getCustomerName()));
            w.newLine();
            w.write(String.format(" Date/Time : %s", order.getTimestamp().format(DISPLAY_DTF)));
            w.newLine();
            w.write("--------------------------------------------");
            w.newLine();
            w.write(String.format(Locale.US, " %-24s %4s %10s", "ITEM", "QTY", "AMOUNT"));
            w.newLine();
            w.write("--------------------------------------------");
            w.newLine();

            for (CartItem item : order.getCart().getItems()) {
                String name = item.getProduct().getName();
                if (name.length() > 24) name = name.substring(0, 22) + "..";
                double lineTotal = item.getProduct().getPrice() * item.getQuantity();
                w.write(String.format(Locale.US, " %-24s %4d %10s",
                    name,
                    item.getQuantity(),
                    String.format(Locale.US, "$%.2f", lineTotal)));
                w.newLine();
                if (item.getQuantity() > 1) {
                    w.write(String.format(Locale.US,
                        "   @ $%.2f each", item.getProduct().getPrice()));
                    w.newLine();
                }
            }

            w.write("--------------------------------------------");
            w.newLine();
            w.write(String.format(Locale.US, " %-30s %10s",
                "TOTAL:", String.format(Locale.US, "$%.2f", order.getFinalTotal())));
            w.newLine();
            w.write("============================================");
            w.newLine();

            System.out.println("Order saved to file: " + fileName);
        } catch (IOException e) {
            System.err.println("Warning: Could not save order file — " + e.getMessage());
        }
    }
}
