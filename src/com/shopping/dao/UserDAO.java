package com.shopping.dao;

import com.shopping.model.Role;
import com.shopping.model.User;
import com.shopping.util.DatabaseConnection;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class UserDAO {

    private Connection connection;

    public UserDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes());
            byte[] encodedhash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not found", e);
        }
    }

    public boolean registerUser(String username, String plainTextPassword, Role role) {
        String query = "INSERT INTO Users (username, password_hash, salt, role) VALUES (?, ?, ?, ?)";
        String salt = generateSalt();
        String hashedPassword = hashPassword(plainTextPassword, salt);

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, salt);
            stmt.setString(4, role.name());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public User authenticate(String username, String plainTextPassword) {
        String query = "SELECT id, username, password_hash, salt, role FROM Users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String dbSalt = rs.getString("salt");
                    String dbHash = rs.getString("password_hash");
                    
                    String hashedInput = hashPassword(plainTextPassword, dbSalt);
                    
                    if (dbHash.equals(hashedInput)) {
                        return new User(rs.getInt("id"), rs.getString("username"), dbHash, Role.valueOf(rs.getString("role")));
                    }
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return null;
    }

    /**
     * Retrieves all registered users (without exposing password hashes).
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT id, username, role FROM Users ORDER BY id";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    "****",
                    Role.valueOf(rs.getString("role"))
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
        }
        return users;
    }

    /**
     * Returns the total number of registered users.
     */
    public int getUserCount() {
        String query = "SELECT COUNT(*) FROM Users";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting users: " + e.getMessage());
        }
        return 0;
    }
}
