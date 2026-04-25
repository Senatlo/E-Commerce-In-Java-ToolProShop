package com.shopping.model;

/**
 * A tangible product that requires physical shipping.
 *
 * <p>Adds {@code shippingWeightKg} and {@code dimensions} on top
 * of the base {@link Product} attributes.</p>
 */
public class PhysicalProduct extends Product {

    private final double shippingWeightKg;
    private final String dimensions; // e.g. "30×20×10 cm"

    /**
     * Creates a new PhysicalProduct.
     *
     * @param id               unique product identifier
     * @param name             product name
     * @param price            unit price
     * @param stockQuantity    initial stock
     * @param shippingWeightKg shipping weight in kilograms (must be > 0)
     * @param dimensions       box dimensions as a human-readable string
     */
    public PhysicalProduct(String id, String name, double price,
                           int stockQuantity, double shippingWeightKg,
                           String dimensions) {
        super(id, name, price, stockQuantity);
        if (shippingWeightKg <= 0) {
            throw new IllegalArgumentException("Shipping weight must be positive.");
        }
        this.shippingWeightKg = shippingWeightKg;
        this.dimensions = dimensions;
    }

    /** @return shipping weight in kg */
    public double getShippingWeightKg() {
        return shippingWeightKg;
    }

    /** @return box dimensions */
    public String getDimensions() {
        return dimensions;
    }

    /**
     * Displays all details including shipping weight and dimensions.
     * Overrides {@link Product#displayDetails()} — <b>Polymorphism</b>.
     */
    @Override
    public void displayDetails() {
        printCommonDetails();
        System.out.printf( "║  Type  : %-32s ║%n", "Physical Product");
        System.out.printf( "║  Weight: %-29.2f kg ║%n", shippingWeightKg);
        System.out.printf( "║  Dims  : %-32s ║%n", dimensions);
        System.out.println("╚══════════════════════════════════════════╝");
    }
}
