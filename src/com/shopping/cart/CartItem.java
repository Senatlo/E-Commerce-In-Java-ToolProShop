package com.shopping.cart;

import com.shopping.model.Product;

/**
 * Represents an item in the shopping cart.
 * Holds a reference to a Product and the quantity to be purchased.
 */
public class CartItem {
    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void addQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to add must be greater than zero.");
        }
        this.quantity += amount;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        this.quantity = quantity;
    }
}
