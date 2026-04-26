package com.shopping.cart;

import com.shopping.dao.ProductDAO;
import com.shopping.model.Product;
import com.shopping.util.CustomLinkedList;

/**
 * The shopping cart managing CartItem objects.
 * Uses CustomLinkedList instead of built-in collections.
 */
public class ShoppingCart {

    private final CustomLinkedList<CartItem> items;
    private final ProductDAO productDAO;
    private double discountPercent = 0.0;

    public ShoppingCart(ProductDAO productDAO) {
        // Intentionally avoiding built-in ArrayList or LinkedList
        this.items = new CustomLinkedList<>();
        this.productDAO = productDAO;
    }

    /**
     * Exposes the custom item list for checkout processing.
     */
    public CustomLinkedList<CartItem> getItems() {
        return items;
    }

    /**
     * Adds a product to the cart. Checks stock availability first via ProductDAO.
     * 
     * @param productId the ID of the product to add
     * @param quantity the quantity to add
     */
    public void addProduct(String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }

        Product product = productDAO.getAllProducts().get(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        // Check if item already exists in the cart to aggregate quantities
        CartItem existingItem = null;
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                existingItem = item;
                break;
            }
        }

        int requestedTotalQty = (existingItem != null ? existingItem.getQuantity() : 0) + quantity;

        // Check stock availability without permanently reducing memory
        if (requestedTotalQty > product.getStockQuantity()) {
            throw new com.shopping.exception.OutOfStockException(productId, requestedTotalQty, product.getStockQuantity());
        }

        if (existingItem != null) {
            existingItem.addQuantity(quantity);
            System.out.println("Updated quantity of '" + product.getName() + "' in cart to " + existingItem.getQuantity() + ".");
        } else {
            items.addNode(new CartItem(product, quantity));
            System.out.println("Added " + quantity + " x '" + product.getName() + "' to cart.");
        }
    }

    /**
     * Removes a product entirely from the cart.
     * 
     * @param productId the ID of the product to remove
     */
    public void removeProduct(String productId) {
        CartItem toRemove = null;
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                toRemove = item;
                break;
            }
        }

        if (toRemove != null) {
            items.removeNode(toRemove);
            System.out.printf("Removed '%s' from the cart.%n", toRemove.getProduct().getName());
        } else {
            System.out.printf("Product %s not found in the cart.%n", productId);
        }
    }

    /**
     * Reduces a cart item's quantity by 1.
     * Automatically removes the item when quantity reaches zero.
     *
     * @param productId the ID of the product to reduce
     */
    public void reduceProduct(String productId) {
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                if (item.getQuantity() <= 1) {
                    removeProduct(productId);
                } else {
                    item.setQuantity(item.getQuantity() - 1);
                    System.out.printf("Reduced '%s' to %d in cart.%n",
                        item.getProduct().getName(), item.getQuantity());
                }
                return;
            }
        }
    }

    /**
     * Calculates the subtotal of the cart.
     * 
     * @return the sequential sum cost of all items in the linked list
     */
    public double calculateSubtotal() {
        double subtotal = 0.0;
        for (CartItem item : items) {
            subtotal += item.getProduct().getPrice() * item.getQuantity();
        }
        return subtotal;
    }

    /**
     * Sets the active discount percentage for the cart.
     */
    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }

    /**
     * Matches the 'calculate_total' requirement.
     * Calculates the final total by applying the active discount to the subtotal.
     * 
     * @return the final total amount
     */
    public double calculateTotal() {
        double subtotal = calculateSubtotal();
        if (discountPercent > 0) {
            return subtotal * (1.0 - (discountPercent / 100.0));
        }
        return subtotal;
    }

    /**
     * Helper to display cart contents safely cleanly.
     */
    public void displayCart() {
        System.out.println("\n========== SHOPPING CART ==========");
        if (items.size() == 0) {
            System.out.println("[ Appears to be empty. ]");
        } else {
            for (CartItem item : items) {
                System.out.printf("| %-25s | Qty: %-4d | Subtotal: $%.2f%n", 
                        item.getProduct().getName(), 
                        item.getQuantity(), 
                        item.getProduct().getPrice() * item.getQuantity());
            }
            double subtotal = calculateSubtotal();
            double total = calculateTotal();
            System.out.println("+------------------------------------------------------+");
            System.out.printf("| SUBTOTAL: $%.2f%n", subtotal);
            if (discountPercent > 0) {
                System.out.printf("| DISCOUNT APPLIED (%s%%): -$%.2f%n", discountPercent, (subtotal - total));
            }
            System.out.printf("| TOTAL ESTIMATE: $%.2f%n", total);
        }
        System.out.println("===================================\n");
    }
}
