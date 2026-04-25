package com.shopping.discount;

/**
 * A concrete strategy that applies a percentage off the cart total.
 */
public class PercentageDiscount implements DiscountStrategy {
    
    private final double percentage;

    public PercentageDiscount(double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100.");
        }
        this.percentage = percentage;
    }

    @Override
    public double applyDiscount(double totalAmount) {
        return Math.max(0, totalAmount - (totalAmount * (percentage / 100.0)));
    }
}
