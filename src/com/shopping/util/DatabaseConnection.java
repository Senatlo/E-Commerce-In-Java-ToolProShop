package com.shopping.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    private static final String URL = "jdbc:h2:~/shopping_db;AUTO_SERVER=TRUE"; 
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private DatabaseConnection() {
        try {
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Failed to establish database connection.");
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void initializeDatabase() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS Users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "username VARCHAR(255) NOT NULL UNIQUE, "
                + "password_hash VARCHAR(255) NOT NULL, "
                + "salt VARCHAR(255) NOT NULL, "
                + "role VARCHAR(50) DEFAULT 'USER'"
                + ");";

        String createProductsTable = "CREATE TABLE IF NOT EXISTS Products ("
                + "id VARCHAR(50) PRIMARY KEY, "
                + "name VARCHAR(255) NOT NULL, "
                + "type VARCHAR(100) NOT NULL, "
                + "price DECIMAL(10, 2) NOT NULL, "
                + "stock INT NOT NULL DEFAULT 0, "
                + "extra_attributes TEXT, "
                + "image_path VARCHAR(255)"
                + ");";

        String createOrdersTable = "CREATE TABLE IF NOT EXISTS Orders ("
                + "id VARCHAR(255) PRIMARY KEY, "
                + "user_id INT NOT NULL, "
                + "total_amount DECIMAL(10, 2) NOT NULL, "
                + "discount_info VARCHAR(500), "
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (user_id) REFERENCES Users(id)"
                + ");";

        String createOrderItemsTable = "CREATE TABLE IF NOT EXISTS Order_Items ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "order_id VARCHAR(255) NOT NULL, "
                + "product_id VARCHAR(50) NOT NULL, "
                + "quantity INT NOT NULL, "
                + "price_at_purchase DECIMAL(10, 2) NOT NULL, "
                + "FOREIGN KEY (order_id) REFERENCES Orders(id), "
                + "FOREIGN KEY (product_id) REFERENCES Products(id)"
                + ");";

        String createDiscountSettingsTable = "CREATE TABLE IF NOT EXISTS Discount_Settings ("
                + "id VARCHAR(50) PRIMARY KEY, "
                + "label VARCHAR(255) NOT NULL, "
                + "discount_percent DOUBLE DEFAULT 0, "
                + "min_order_value DOUBLE DEFAULT 0, "
                + "min_items INT DEFAULT 0, "
                + "enabled BOOLEAN DEFAULT FALSE"
                + ");";

        try (Statement statement = getConnection().createStatement()) {
            statement.execute(createUsersTable);
            statement.execute(createProductsTable);
            statement.execute(createOrdersTable);
            statement.execute(createOrderItemsTable);
            statement.execute(createDiscountSettingsTable);
            System.out.println("Enhanced Advanced Schema initialized successfully.");
            
            // Add a shutdown hook to ensure the database connection is closed gracefully
            Runtime.getRuntime().addShutdownHook(new Thread(this::closeConnection));
            
        } catch (SQLException e) {
            System.err.println("Failed to initialize database tables.");
            e.printStackTrace();
        }
    }

    /**
     * Safely closes the database connection.
     * Especially important for H2 to ensure data is flushed and lock files are removed.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed gracefully.");
            }
        } catch (SQLException e) {
            System.err.println("Error while closing database connection.");
            e.printStackTrace();
        }
    }
}
