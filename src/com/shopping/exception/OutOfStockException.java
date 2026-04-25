package com.shopping.exception;

/**
 * Custom unchecked exception thrown when an attempt is made
 * to reduce a product's stock below zero.
 *
 * <p>By extending {@link RuntimeException}, callers are not forced
 * to declare or catch it — but they absolutely <em>should</em> handle
 * it at the service / UI boundary.</p>
 */
public class OutOfStockException extends RuntimeException {

    private final String productId;
    private final int requestedQuantity;
    private final int availableQuantity;

    /**
     * Constructs a new OutOfStockException.
     *
     * @param productId         the ID of the product that is out of stock
     * @param requestedQuantity the quantity that was requested
     * @param availableQuantity the quantity currently available
     */
    public OutOfStockException(String productId,
                               int requestedQuantity,
                               int availableQuantity) {
        super(String.format(
                "Out of stock — Product [%s]: requested %d, but only %d available.",
                productId, requestedQuantity, availableQuantity));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    /** @return the ID of the affected product */
    public String getProductId() {
        return productId;
    }

    /** @return the quantity that was requested */
    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    /** @return the quantity that was actually available */
    public int getAvailableQuantity() {
        return availableQuantity;
    }
}
