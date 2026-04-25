package com.shopping.dao;

import com.shopping.model.DigitalProduct;
import com.shopping.model.PerishableProduct;
import com.shopping.model.PhysicalProduct;
import com.shopping.model.Product;
import com.shopping.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductDAO {

    private Connection connection;

    public ProductDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public Connection getConnection() {
        return this.connection;
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String name = rs.getString("name");
        String type = rs.getString("type");
        double price = rs.getDouble("price");
        int stock = rs.getInt("stock");
        String extraAttr = rs.getString("extra_attributes");
        String imagePath = rs.getString("image_path");

        Product product;
        if ("PhysicalProduct".equals(type)) {
            product = new PhysicalProduct(id, name, price, stock, 1.0, extraAttr != null ? extraAttr : "");
        } else if ("DigitalProduct".equals(type)) {
            product = new DigitalProduct(id, name, price, stock, extraAttr != null ? extraAttr : "", 5.0);
        } else if ("PerishableProduct".equals(type)) {
            product = new PerishableProduct(id, name, price, stock, LocalDate.now().plusDays(10), 0.0);
        } else {
            product = new PhysicalProduct(id, name, price, stock, 1.0, ""); 
        }
        product.setImagePath(imagePath);
        return product;
    }

    public Map<String, Product> getAllProducts() {
        Map<String, Product> products = new LinkedHashMap<>();
        String query = "SELECT id, name, type, price, stock, extra_attributes, image_path FROM Products ORDER BY id";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Product product = mapResultSetToProduct(rs);
                products.put(product.getId(), product);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching products from DB: " + e.getMessage());
        }
        return products;
    }

    /**
     * Executes SELECT ... FOR UPDATE enforcing row-level Mutex Locking inside a transaction context.
     */
    public Product getProductForUpdate(String id) throws SQLException {
        String query = "SELECT id, name, type, price, stock, extra_attributes, image_path FROM Products WHERE id = ? FOR UPDATE";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        }
        return null;
    }

    public void addProduct(Product p) {
        String query = "INSERT INTO Products (id, name, type, price, stock, extra_attributes, image_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, p.getId());
            stmt.setString(2, p.getName());
            
            String type = p.getClass().getSimpleName();
            stmt.setString(3, type);
            stmt.setDouble(4, p.getPrice());
            stmt.setInt(5, p.getStockQuantity());
            
            String extra = "";
            if (p instanceof PhysicalProduct) extra = ((PhysicalProduct)p).getDimensions();
            else if (p instanceof DigitalProduct) extra = ((DigitalProduct)p).getDownloadLink();
            else if (p instanceof PerishableProduct) extra = "Expires next week";
            
            stmt.setString(6, extra);
            stmt.setString(7, p.getImagePath());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
        }
    }

    public void updateStock(String productId, int newStock) throws SQLException {
        String updateQuery = "UPDATE Products SET stock = ? WHERE id = ?";
        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
            updateStmt.setInt(1, newStock);
            updateStmt.setString(2, productId);
            updateStmt.executeUpdate();
        }
    }

    /**
     * Updates all editable fields of a product in the database.
     */
    public void updateProduct(Product p) {
        String query = "UPDATE Products SET name = ?, price = ?, stock = ?, image_path = ?, extra_attributes = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, p.getName());
            stmt.setDouble(2, p.getPrice());
            stmt.setInt(3, p.getStockQuantity());
            stmt.setString(4, p.getImagePath());

            String extra = "";
            if (p instanceof PhysicalProduct) extra = ((PhysicalProduct)p).getDimensions();
            else if (p instanceof DigitalProduct) extra = ((DigitalProduct)p).getDownloadLink();
            else if (p instanceof PerishableProduct) extra = "Expires next week";
            stmt.setString(5, extra);

            stmt.setString(6, p.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
        }
    }

    /**
     * Deletes a product from the database by its ID.
     */
    public boolean deleteProduct(String productId) {
        String query = "DELETE FROM Products WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting product: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a product with the given ID already exists.
     */
    public boolean productExists(String productId) {
        String query = "SELECT 1 FROM Products WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  DISCOUNT SETTINGS MANAGEMENT
    // ══════════════════════════════════════════════════════════════

    /**
     * Represents a single discount configuration row.
     */
    public static class DiscountSetting {
        public String id;
        public String label;
        public double discountPercent;
        public double minOrderValue;
        public int minItems;
        public boolean enabled;

        public DiscountSetting(String id, String label, double discountPercent,
                                double minOrderValue, int minItems, boolean enabled) {
            this.id = id;
            this.label = label;
            this.discountPercent = discountPercent;
            this.minOrderValue = minOrderValue;
            this.minItems = minItems;
            this.enabled = enabled;
        }
    }

    /**
     * Seeds default discount settings if the table is empty.
     */
    public void seedDefaultDiscounts() {
        String check = "SELECT COUNT(*) FROM Discount_Settings";
        try (PreparedStatement stmt = connection.prepareStatement(check);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) return; // Already seeded
        } catch (SQLException e) { return; }

        String insert = "INSERT INTO Discount_Settings (id, label, discount_percent, min_order_value, min_items, enabled) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insert)) {
            // Season Sale
            stmt.setString(1, "SEASON_SALE");
            stmt.setString(2, "Season Sale");
            stmt.setDouble(3, 15.0);
            stmt.setDouble(4, 0);
            stmt.setInt(5, 0);
            stmt.setBoolean(6, false);
            stmt.addBatch();

            // Loyalty / User Discount
            stmt.setString(1, "USER_DISCOUNT");
            stmt.setString(2, "User Loyalty Discount");
            stmt.setDouble(3, 10.0);
            stmt.setDouble(4, 50.0);
            stmt.setInt(5, 0);
            stmt.setBoolean(6, false);
            stmt.addBatch();

            // Bulk Order Discount
            stmt.setString(1, "BULK_DISCOUNT");
            stmt.setString(2, "Bulk Order Discount");
            stmt.setDouble(3, 20.0);
            stmt.setDouble(4, 0);
            stmt.setInt(5, 5);
            stmt.setBoolean(6, false);
            stmt.addBatch();

            // Clearance Sale
            stmt.setString(1, "CLEARANCE");
            stmt.setString(2, "Clearance Sale");
            stmt.setDouble(3, 30.0);
            stmt.setDouble(4, 0);
            stmt.setInt(5, 0);
            stmt.setBoolean(6, false);
            stmt.addBatch();

            // Holiday Special
            stmt.setString(1, "HOLIDAY");
            stmt.setString(2, "Holiday Special");
            stmt.setDouble(3, 25.0);
            stmt.setDouble(4, 100.0);
            stmt.setInt(5, 0);
            stmt.setBoolean(6, false);
            stmt.addBatch();

            stmt.executeBatch();
        } catch (SQLException e) {
            System.err.println("Error seeding discount settings: " + e.getMessage());
        }
    }

    /**
     * Retrieves all discount settings.
     */
    public List<DiscountSetting> getAllDiscountSettings() {
        List<DiscountSetting> settings = new ArrayList<>();
        String query = "SELECT id, label, discount_percent, min_order_value, min_items, enabled FROM Discount_Settings ORDER BY id";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                settings.add(new DiscountSetting(
                    rs.getString("id"),
                    rs.getString("label"),
                    rs.getDouble("discount_percent"),
                    rs.getDouble("min_order_value"),
                    rs.getInt("min_items"),
                    rs.getBoolean("enabled")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching discount settings: " + e.getMessage());
        }
        return settings;
    }

    /**
     * Updates a discount setting (percent, conditions, enabled state).
     */
    public void updateDiscountSetting(DiscountSetting ds) {
        String query = "UPDATE Discount_Settings SET label = ?, discount_percent = ?, min_order_value = ?, min_items = ?, enabled = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, ds.label);
            stmt.setDouble(2, ds.discountPercent);
            stmt.setDouble(3, ds.minOrderValue);
            stmt.setInt(4, ds.minItems);
            stmt.setBoolean(5, ds.enabled);
            stmt.setString(6, ds.id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating discount setting: " + e.getMessage());
        }
    }

    /**
     * Returns all enabled discount settings that apply to the given order.
     */
    public List<DiscountSetting> getApplicableDiscounts(double orderTotal, int itemCount) {
        List<DiscountSetting> applicable = new ArrayList<>();
        for (DiscountSetting ds : getAllDiscountSettings()) {
            if (ds.enabled && orderTotal >= ds.minOrderValue && itemCount >= ds.minItems) {
                applicable.add(ds);
            }
        }
        return applicable;
    }
}
