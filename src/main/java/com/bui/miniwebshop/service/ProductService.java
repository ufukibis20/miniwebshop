package com.bui.miniwebshop.service;

import com.bui.miniwebshop.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
public class ProductService {
    private final String DB_URL = "jdbc:sqlite:webshop.db";

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM product")) {

            while (rs.next()) {
                Product p = new Product(
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("category"),
                        rs.getInt("stock")
                );
                products.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public void updateStock(String productName, int newStock) {
        String sql = "UPDATE product SET stock = ? WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newStock);
            pstmt.setString(2, productName);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addProduct(Product product) {
        String sql = "INSERT INTO product (name, price, stock, category) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getStock());
            pstmt.setString(4, product.getCategory());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateProduct(Product product) {
        String sql = "UPDATE product SET price = ?, stock = ?, category = ? WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, product.getPrice());
            pstmt.setInt(2, product.getStock());
            pstmt.setString(3, product.getCategory());
            pstmt.setString(4, product.getName());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteProductByName(String productName) {
        String sql = "DELETE FROM product WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productName);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
