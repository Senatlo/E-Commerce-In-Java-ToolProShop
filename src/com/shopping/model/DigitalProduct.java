package com.shopping.model;

/**
 * A product delivered electronically (software, e-books, music, etc.).
 *
 * <p>Adds {@code downloadLink} and {@code fileSizeMB} to the base
 * {@link Product} attributes.  Digital products typically have
 * "unlimited" stock, but we still honour the stock field for
 * license-limited scenarios.</p>
 */
public class DigitalProduct extends Product {

    private final String downloadLink;
    private final double fileSizeMB;

    /**
     * Creates a new DigitalProduct.
     *
     * @param id            unique product identifier
     * @param name          product name
     * @param price         unit price
     * @param stockQuantity initial stock (use {@link Integer#MAX_VALUE} for unlimited)
     * @param downloadLink  URL / URI where the buyer can download the product
     * @param fileSizeMB    file size in megabytes
     */
    public DigitalProduct(String id, String name, double price,
                          int stockQuantity, String downloadLink,
                          double fileSizeMB) {
        super(id, name, price, stockQuantity);
        this.downloadLink = downloadLink;
        this.fileSizeMB = fileSizeMB;
    }

    /** @return download URL */
    public String getDownloadLink() {
        return downloadLink;
    }

    /** @return file size in MB */
    public double getFileSizeMB() {
        return fileSizeMB;
    }

    /**
     * Displays all details including the download link and file size.
     * Overrides {@link Product#displayDetails()} — <b>Polymorphism</b>.
     */
    @Override
    public void displayDetails() {
        printCommonDetails();
        System.out.printf( "║  Type  : %-32s ║%n", "Digital Product");
        System.out.printf( "║  Link  : %-32s ║%n", downloadLink);
        System.out.printf( "║  Size  : %-29.1f MB ║%n", fileSizeMB);
        System.out.println("╚══════════════════════════════════════════╝");
    }
}
