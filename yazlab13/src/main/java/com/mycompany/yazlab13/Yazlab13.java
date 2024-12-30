package com.mycompany.yazlab13;

import java.sql.*;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Yazlab13 {
    private static final Logger LOGGER = Logger.getLogger(Yazlab13.class.getName());

    // Veritabanı bağlantısı için bilgiler
    private static final String URL = "jdbc:mysql://localhost:3306/yazlab13";
    private static final String USER = "admin";
    private static final String PASSWORD = "admin";

    public static void main(String[] args) {
        try {
            // Veritabanı sürücüsünü yükle
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Sürücüsü bulunamadı", e);
            return;
        }

        giris giris = new giris();
        giris.setVisible(true);
        Random random = new Random();

        int customerCount = random.nextInt(6) + 5;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            LOGGER.info("Veritabanı bağlantısı başarılı!");

            // Premium müşterileri ekle
            addCustomers(conn, customerCount);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Veritabanı bağlantı hatası", e);
        }
    }

    private static void addCustomers(Connection conn, int customerCount) throws SQLException {
        Random random = new Random();

        // Premium müşterileri ekle
        for (int i = 0; i < 2; i++) {
            String customerName = "P" + (i + 1);
            addCustomer(conn, 
                customerName, 
                "P " + (i + 1), 
                random.nextInt(2501) + 500, 
                "Premium"
            );
        }

        // Diğer müşterileri ekle
        for (int i = 2; i < customerCount; i++) {
            String customerName = "C" + (i + 1);
            addCustomer(conn, 
                customerName, 
                "C" + (i + 1), 
                random.nextInt(2501) + 500, 
                "Regular"
            );
        }

        LOGGER.info(customerCount + " müşteri başarıyla eklendi.");
    }

    private static void addCustomer(Connection conn, String customerName, String displayName, 
                                    double budget, String customerType) throws SQLException {
        // Müşteri ID oluşturma
        String customerId = generateCustomerId(conn, customerName);
        
        // Müşteri var mı kontrolü
        String checkSql = "SELECT COUNT(*) FROM app_customer WHERE customer_name = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, displayName);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    LOGGER.warning("Müşteri zaten mevcut: " + displayName);
                    return;
                }
            }
        }
        
        // Müşteri ekleme
        String sql = "INSERT INTO app_customer (customer_id, customer_name, budget, customer_type, total_spent) " +
                     "VALUES (?, ?, ?, ?, 0)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            pstmt.setString(2, displayName);
            pstmt.setDouble(3, budget);
            pstmt.setString(4, customerType);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info("Müşteri eklendi: " + displayName + ", ID: " + customerId);
            } else {
                LOGGER.warning("Müşteri eklenemedi: " + displayName);
            }
        }
    }

    private static String generateCustomerId(Connection conn, String customerName) throws SQLException {
        // Güvenli ID oluşturma
        String prefix = customerName.substring(0, Math.min(2, customerName.length())).toUpperCase();
        
        int attempts = 0;
        while (attempts < 100) { // Sonsuz döngüyü önle
            String customerId = prefix + String.format("%04d", new Random().nextInt(9999));
            
            // ID'nin benzersiz olup olmadığını kontrol et
            String checkSql = "SELECT COUNT(*) FROM app_customer WHERE customer_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, customerId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        return customerId; // Benzersiz ID bulundu
                    }
                }
            }
            attempts++;
        }
        
        // Benzersiz ID bulunamazsa
        throw new SQLException("Benzersiz müşteri ID'si oluşturulamadı");
    }

    // Ürün listeleme metodu
    public void listProducts() {
        String query = "SELECT * FROM app_urun";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                LOGGER.info(String.format("ID: %d, Name: %s, Stock: %d, Price: %.2f", 
                    rs.getInt("id"), 
                    rs.getString("productName"),
                    rs.getInt("stok"), 
                    rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ürünleri listelerken hata oluştu", e);
        }
    }

    // Ürün ekleme metodu
    public static void addProduct(String urun_id, String productName, int stok, double price) {
        String query = "INSERT INTO app_urun (urun_id, productName, stok, price) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, urun_id);
            pstmt.setString(2, productName);
            pstmt.setInt(3, stok);
            pstmt.setDouble(4, price);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info("Yeni ürün başarıyla eklendi: " + productName);
            } else {
                LOGGER.warning("Ürün eklenemedi: " + productName);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ürün eklenirken hata oluştu", e);
        }
    }

    // Ürün silme metodu
    public void deleteProduct(String productName) {
        String query = "DELETE FROM app_urun WHERE productName = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, productName);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                LOGGER.info(productName + " başarıyla silindi.");
            } else {
                LOGGER.warning(productName + " bulunamadı.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ürün silinirken hata oluştu", e);
        }
    }

    // Stok güncelleme metodu
    public void updateStock(int productId, int quantity) {
        String query = "UPDATE app_urun SET stok = stok + ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, quantity);
            pstmt.setInt(2, productId);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info("Stok başarıyla güncellendi.");
            } else {
                LOGGER.warning("Stok güncellenemedi.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Stok güncellenirken hata oluştu", e);
        }
    }

    // Ürün satın alma metodu
    public boolean buyProduct(int productId, int quantity) {
        String query = "SELECT stok FROM app_urun WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int stock = rs.getInt("stok");
                    if (stock >= quantity) {
                        // Stok yeterliyse güncelleme yapılır
                        String updateQuery = "UPDATE app_urun SET stok = stok - ? WHERE id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                            updateStmt.setInt(1, quantity);
                            updateStmt.setInt(2, productId);
                            
                            int rowsAffected = updateStmt.executeUpdate();
                            if (rowsAffected > 0) {
                                LOGGER.info("Ürün başarıyla satın alındı.");
                                return true;
                            } else {
                                LOGGER.warning("Ürün satın alınamadı.");
                                return false;
                            }
                        }
                    } else {
                        LOGGER.warning("Yeterli stok yok.");
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ürün satın alınırken hata oluştu", e);
        }
        return false;
    }
}