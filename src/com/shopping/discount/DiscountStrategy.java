package com.shopping.discount;

/**
 * Strategy interface to implement a flexible discount system.
 */
public interface DiscountStrategy {
    
    /**
     * Applies the discount rule to the total amount.
     * Matches the 'apply_discount' requirement.
     * 
     * @param totalAmount the initial subtotal amount before discount
     * @return the final total amount after applying the discount
     */
    double applyDiscount(double totalAmount);
}
