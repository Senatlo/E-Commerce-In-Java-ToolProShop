package com.shopping.catalog;

import com.shopping.exception.OutOfStockException;
import com.shopping.model.Product;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages the product catalog — adding, removing, querying,
 * and safely adjusting stock levels.
 *
 * <p>The catalog is backed by a {@link LinkedHashMap} so iteration
 * order matches insertion order (nice for display).</p>
 */
public class ProductManager {

    /** Internal catalog: product-ID → Product */
    private final Map<String, Product> catalog = new LinkedHashMap<>();

    // ── Catalog CRUD ─────────────────────────────────────────────────

    /**
     * Adds a product to the catalog.
     *
     * @param product the product to add
     * @throws IllegalArgumentException if a product with the same ID already exists
     */
    public void addProduct(Product product) {
        if (catalog.containsKey(product.getId())) {
            throw new IllegalArgumentException(
                    "Duplicate product ID: " + product.getId());
        }
        catalog.put(product.getId(), product);
    }

    /**
     * Removes a product from the catalog by its ID.
     *
     * @param productId the ID to remove
     * @return {@code true} if the product was found and removed
     */
    public boolean removeProduct(String productId) {
        return catalog.remove(productId) != null;
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param productId the ID to look up
     * @return an {@link Optional} containing the product, or empty if not found
     */
    public Optional<Product> getProduct(String productId) {
        return Optional.ofNullable(catalog.get(productId));
    }

    /**
     * Returns an <em>unmodifiable</em> view of the entire catalog.
     *
     * @return read-only map of product-ID → Product
     */
    public Map<String, Product> getAllProducts() {
        return Collections.unmodifiableMap(catalog);
    }

    // ── Stock management ─────────────────────────────────────────────

    /**
     * Safely decreases the stock of an existing product.
     *
     * <p><b>Edge-case handling:</b> If the requested quantity exceeds
     * the available stock, an {@link OutOfStockException} is thrown
     * and the stock remains unchanged (no partial deduction).</p>
     *
     * @param productId the ID of the product
     * @param quantity  the number of units to deduct (must be &gt; 0)
     * @throws IllegalArgumentException if quantity ≤ 0 or product not found
     * @throws OutOfStockException      if available stock is insufficient
     */
    public void decreaseStock(String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity to decrease must be positive; got " + quantity);
        }

        Product product = catalog.get(productId);
        if (product == null) {
            throw new IllegalArgumentException(
                    "Product not found: " + productId);
        }

        int currentStock = product.getStockQuantity();
        if (quantity > currentStock) {
            throw new OutOfStockException(productId, quantity, currentStock);
        }

        // Safe to deduct — both classes are in the same package,
        // so the package-private setter is accessible here.
        product.setStockQuantity(currentStock - quantity);
    }

    // ── Display ──────────────────────────────────────────────────────

    /**
     * Iterates over every product and calls {@link Product#displayDetails()}.
     * Because each subclass overrides the method, <b>polymorphism</b>
     * ensures the correct version is invoked at runtime.
     */
    public void displayAllProducts() {
        if (catalog.isEmpty()) {
            System.out.println("[ The catalog is empty. ]");
            return;
        }
        System.out.println("\n========== PRODUCT CATALOG ==========\n");
        for (Product product : catalog.values()) {
            product.displayDetails();   // ← polymorphic call
            System.out.println();
        }
        System.out.println("=====================================\n");
    }
}
