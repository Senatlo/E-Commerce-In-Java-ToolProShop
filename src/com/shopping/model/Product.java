package com.shopping.model;

/**
 * Abstract base class for every item in the product catalog.
 *
 * <p><b>Encapsulation:</b> All fields are {@code private}; access is
 * controlled through getters (and a package-private stock setter used
 * only by {@link com.shopping.catalog.ProductManager}).</p>
 *
 * <p><b>Polymorphism:</b> Subclasses <em>must</em> override
 * {@link #displayDetails()} to present their own unique attributes.</p>
 */
public abstract class Product {

    // ── Encapsulated attributes ──────────────────────────────────────
    private final String id;
    private final String name;
    private double price;
    private int stockQuantity;
    private String imagePath;

    // ── Constructor ──────────────────────────────────────────────────

    /**
     * Protected constructor — only subclasses may instantiate.
     *
     * @param id            unique product identifier
     * @param name          human-readable product name
     * @param price         unit price (must be ≥ 0)
     * @param stockQuantity initial stock (must be ≥ 0)
     * @throws IllegalArgumentException if price or stock is negative
     */
    protected Product(String id, String name, double price, int stockQuantity) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        this.id = id;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imagePath = null;
    }

    // ── Getters ──────────────────────────────────────────────────────

    /** @return unique product identifier */
    public String getId() {
        return id;
    }

    /** @return product name */
    public String getName() {
        return name;
    }

    /** @return unit price */
    public double getPrice() {
        return price;
    }

    /** @return current stock quantity */
    public int getStockQuantity() {
        return stockQuantity;
    }

    /** @return relative path to the product image asset */
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // ── Controlled mutators ──────────────────────────────────────────

    /**
     * Updates the unit price.
     *
     * @param price new price (must be ≥ 0)
     * @throws IllegalArgumentException if price is negative
     */
    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        this.price = price;
    }

    /**
     * Sets the stock quantity directly.
     *
     * <p><b>Important:</b> This method is intentionally {@code public} so
     * that {@link com.shopping.catalog.ProductManager} (which lives in a
     * different package) can update stock after performing its own
     * validation.  Client code should <em>always</em> go through
     * {@code ProductManager.decreaseStock()} rather than calling this
     * method directly.</p>
     *
     * @param stockQuantity new stock level (must be ≥ 0)
     * @throws IllegalArgumentException if the value is negative
     */
    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        this.stockQuantity = stockQuantity;
    }

    // ── Abstract method — the polymorphism hook ──────────────────────

    /**
     * Prints product-specific details to {@code stdout}.
     * Each concrete subclass provides its own implementation.
     */
    public abstract void displayDetails();

    // ── Common helpers ───────────────────────────────────────────────

    /**
     * Shared header printed by every subclass before its own fields.
     * Keeps output consistent across product types.
     */
    protected void printCommonDetails() {
        System.out.println("+--------------------------------------------+");
        System.out.printf( "|  ID    : %-32s  |%n", id);
        System.out.printf( "|  Name  : %-32s  |%n", name);
        System.out.printf( "|  Price : $%-31.2f  |%n", price);
        System.out.printf( "|  Stock : %-32d  |%n", stockQuantity);
    }

    @Override
    public String toString() {
        return String.format("Product{id='%s', name='%s', price=%.2f, stock=%d}",
                id, name, price, stockQuantity);
    }
}
