package com.shopping.discount;

/**
 * A concrete strategy that applies a fixed flat rate discount,
 * optionally only if the order exceeds a certain minimum value.
 */
public class FlatRateDiscount implements DiscountStrategy {
    
    private final double flatRate;
    private final double minimumOrderValue;

    public FlatRateDiscount(double flatRate, double minimumOrderValue) {
        if (flatRate < 0) {
            throw new IllegalArgumentException("Flat rate discount cannot be negative.");
        }
        this.flatRate = flatRate;
        this.minimumOrderValue = minimumOrderValue;
    }

    @Override
    public double applyDiscount(double totalAmount) {
        if (totalAmount >= minimumOrderValue) {
            return Math.max(0, totalAmount - flatRate);
        }
        return totalAmount;
    }
}
