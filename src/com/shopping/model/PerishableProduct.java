package com.shopping.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * A product with a limited shelf life (food, medicine, cosmetics, etc.).
 *
 * <p>Adds {@code expirationDate} and {@code storageTemperatureCelsius}
 * to the base {@link Product} attributes.</p>
 */
public class PerishableProduct extends Product {

    private final LocalDate expirationDate;
    private final double storageTemperatureCelsius;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Creates a new PerishableProduct.
     *
     * @param id                        unique product identifier
     * @param name                      product name
     * @param price                     unit price
     * @param stockQuantity             initial stock
     * @param expirationDate            date after which the product is no longer usable
     * @param storageTemperatureCelsius  recommended storage temperature in °C
     */
    public PerishableProduct(String id, String name, double price,
                             int stockQuantity, LocalDate expirationDate,
                             double storageTemperatureCelsius) {
        super(id, name, price, stockQuantity);
        this.expirationDate = expirationDate;
        this.storageTemperatureCelsius = storageTemperatureCelsius;
    }

    /** @return expiration date */
    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    /** @return recommended storage temperature in Celsius */
    public double getStorageTemperatureCelsius() {
        return storageTemperatureCelsius;
    }

    /** @return {@code true} if the product has already expired */
    public boolean isExpired() {
        return LocalDate.now().isAfter(expirationDate);
    }

    /**
     * Displays all details including expiration date and storage temperature.
     * Overrides {@link Product#displayDetails()} — <b>Polymorphism</b>.
     */
    @Override
    public void displayDetails() {
        printCommonDetails();
        System.out.printf( "║  Type  : %-32s ║%n", "Perishable Product");
        System.out.printf( "║  Expiry: %-32s ║%n", expirationDate.format(DATE_FMT));
        System.out.printf( "║  Temp  : %-29.1f °C ║%n", storageTemperatureCelsius);
        System.out.printf( "║  Status: %-32s ║%n", isExpired() ? "⚠ EXPIRED" : "✓ Fresh");
        System.out.println("╚══════════════════════════════════════════╝");
    }
}
