/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.yazlab13;

import java.math.BigDecimal;
import java.sql.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JOptionPane;

public class ProductPurchaseManager {
    // Veritabanı bağlantı bilgileri
    private static final String DB_URL = "jdbc:mysql://localhost:3306/yazlab13";
    private static final String USER = "admin";
    private static final String PASS = "admin"; // Kendi şifrenizi buraya yazın

    // Ürün listesi için mutex
    private final ReentrantLock productLock = new ReentrantLock();
    
    // Her ürün için ayrı bir semaphore
    private final Semaphore[] productSemaphores;
    
    public ProductPurchaseManager(int numberOfProducts) {
        // Ürün sayısına göre semaphore dizisi oluştur
        productSemaphores = new Semaphore[numberOfProducts];
        
        // Her ürün için semaphore ayarla
        for (int i = 0; i < numberOfProducts; i++) {
            productSemaphores[i] = new Semaphore(1); // Her ürün için 1 izin
        }
    }
    
    // Veritabanı bağlantısı
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
    public boolean purchaseProduct(String customerId, String productId, int quantity) {
    Connection conn = null;
    try {
        System.out.println("Satın alma işlemi başladı:");
        System.out.println("Müşteri ID: " + customerId);
        System.out.println("Ürün ID: " + productId);
        System.out.println("Miktar: " + quantity);
        
        
        // Ürün semaphore'unu kilitle
        productSemaphores[Integer.parseInt(productId.substring(1)) - 1].acquire();
        
        conn = getConnection();
        conn.setAutoCommit(false);
        
        productLock.lock();
        try {
            // Ürün stok ve fiyat kontrolü
            String checkStockQuery = "SELECT stok, price FROM app_urun WHERE urun_id = ?";
            PreparedStatement checkStockStmt = conn.prepareStatement(checkStockQuery);
            checkStockStmt.setString(1, productId);
            ResultSet rs = checkStockStmt.executeQuery();
            
            if (rs.next()) {
                int currentStock = rs.getInt("stok");
                double price = rs.getDouble("price");
                
                System.out.println("Mevcut Stok: " + currentStock);
                System.out.println("Ürün Fiyatı: " + price);
                
                if (currentStock >= quantity) {
                    // Müşteri bütçe kontrolü
                    String checkBudgetQuery = "SELECT budget FROM app_customer WHERE customer_id = ?";
                    PreparedStatement checkBudgetStmt = conn.prepareStatement(checkBudgetQuery);
                    checkBudgetStmt.setString(1, customerId);
                    ResultSet budgetRs = checkBudgetStmt.executeQuery();
                    
                    if (budgetRs.next()) {
                        double customerBudget = budgetRs.getDouble("budget");
                        double totalCost = price * quantity;
                        
                        System.out.println("Müşteri Bütçesi: " + customerBudget);
                        System.out.println("Toplam Maliyet: " + totalCost);
                        
                        if (customerBudget >= totalCost) {
                            // Siparişi oluştur
                           // Siparişi oluştur
String insertOrderQuery = "INSERT INTO orders (customer_id, urun_id , quantity, total_price, status) VALUES (?, ?, ?, ?, 'Pending')";
PreparedStatement insertOrderStmt = conn.prepareStatement(insertOrderQuery);
insertOrderStmt.setString(1, customerId);
insertOrderStmt.setString(2, productId);
insertOrderStmt.setInt(3, quantity);
insertOrderStmt.setDouble(4, totalCost);
insertOrderStmt.executeUpdate();
                            
                            // Stok ve bütçe güncelleme zorunluluğunu kaldır
                            // Bu işlemler admin onayından sonra yapılacak
                            
                            System.out.println("Sipariş admin onayına gönderildi!");
                            conn.commit();
                            return true;
                        } else {
                            System.out.println("Yetersiz bütçe!");
                        }
                    } else {
                        System.out.println("Müşteri bulunamadı!");
                    }
                } else {
                    System.out.println("Yetersiz stok!");
                }
            } else {
                System.out.println("Ürün bulunamadı!");
            }
            
            conn.rollback();
            return false;
        } finally {
            productLock.unlock();
        }
    } catch (SQLException | InterruptedException e) {
        e.printStackTrace();
        return false;
    } finally {
        // Bağlantıyı kapat
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Semaphore'u serbest bırak
        productSemaphores[Integer.parseInt(productId.substring(1)) - 1].release();
    }
}
    
    public void placeOrder(String customerId, String productId, int quantity, double totalPrice) {
    // Veritabanı bağlantı bilgileri
    String URL = "jdbc:mysql://localhost:3306/yazlab13";
    String USER = "admin";
    String PASSWORD = "admin";

    // SQL sorgusu
    String sql = "INSERT INTO orders (customer_id, urun_id, Quantity, TotalPrice, OrderStatus, OrderDate) " +
                 "VALUES (?, ?, ?, ?, ?, NOW())";

    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        // Değerleri SQL sorgusuna bağla
        stmt.setString(1, customerId);
        stmt.setString(2, productId);
        stmt.setInt(3, quantity);
        stmt.setBigDecimal(4, BigDecimal.valueOf(totalPrice));
        stmt.setString(5, "Pending"); // Sipariş durumu 'Pending' olarak ayarlanıyor

        // Sorguyu çalıştır
        int rowsInserted = stmt.executeUpdate();

        if (rowsInserted > 0) {
            JOptionPane.showMessageDialog(null, 
                "Sipariş başarıyla kaydedildi ve onay bekleniyor.", 
                "Başarılı", 
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, 
                "Sipariş kaydedilemedi. Lütfen tekrar deneyin.", 
                "Hata", 
                JOptionPane.ERROR_MESSAGE);
        }

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, 
            "Sipariş verirken bir hata oluştu: " + e.getMessage(), 
            "Hata", 
            JOptionPane.ERROR_MESSAGE);
    }
}
public void adminApproveOrReject(int orderId, boolean isApproved) {
    Connection conn = null;
    String URL = "jdbc:mysql://localhost:3306/yazlab13";
    String USER = "admin";
    String PASSWORD = "admin";
    try {
         conn = DriverManager.getConnection(URL, USER, PASSWORD);
        conn.setAutoCommit(false);
        
        String customerId = ""; // Customer ID değişkeni, işlemin her durumda kullanılacağı yer.
        
        if (isApproved) {
            // Siparişi onaylama
            String orderDetailsQuery = "SELECT customer_id, urun_id, Quantity, TotalPrice FROM orders WHERE OrderID = ?";
            PreparedStatement orderDetailsStmt = conn.prepareStatement(orderDetailsQuery);
            orderDetailsStmt.setInt(1, orderId);
            ResultSet rs = orderDetailsStmt.executeQuery();
            
            if (rs.next()) {
                customerId = rs.getString("customer_id"); // Müşteri ID'sini al
                String productId = rs.getString("urun_id");
                int quantity = rs.getInt("Quantity");
                double totalPrice = rs.getDouble("TotalPrice");
                
                // Ürün stokunu düşür
                String updateStockQuery = "UPDATE app_urun SET stok = stok - ? WHERE urun_id = ?";
                PreparedStatement updateStockStmt = conn.prepareStatement(updateStockQuery);
                updateStockStmt.setInt(1, quantity);
                updateStockStmt.setString(2, productId);
                updateStockStmt.executeUpdate();
                
                // Müşteri bütçesini düşür
                String updateBudgetQuery = "UPDATE app_customer SET budget = budget - ?, total_spent = total_spent + ? WHERE customer_id = ?";
                PreparedStatement updateBudgetStmt = conn.prepareStatement(updateBudgetQuery);
                updateBudgetStmt.setDouble(1, totalPrice);
                updateBudgetStmt.setDouble(2, totalPrice);
                updateBudgetStmt.setString(3, customerId); // Müşteri ID'sini burada kullan
                updateBudgetStmt.executeUpdate();
                
                // Siparişi onaylandı olarak işaretle
                String approveQuery = "UPDATE orders SET OrderStatus = 'Shipped' WHERE OrderID = ?";
                PreparedStatement approveStmt = conn.prepareStatement(approveQuery);
                approveStmt.setInt(1, orderId);
                approveStmt.executeUpdate();
                
                // Log kaydetme (sipariş onayı için)
                String productNameQuery = "SELECT productName FROM app_urun WHERE urun_id = ?";
                PreparedStatement productStmt = conn.prepareStatement(productNameQuery);
                productStmt.setString(1, productId);
                ResultSet productRs = productStmt.executeQuery();
                String productName = "";
                if (productRs.next()) {
                    productName = productRs.getString("productName");
                }

                String customerTypeQuery = "SELECT customer_type FROM app_customer WHERE customer_id = ?";
                PreparedStatement customerStmt = conn.prepareStatement(customerTypeQuery);
                customerStmt.setString(1, customerId);
                ResultSet customerRs = customerStmt.executeQuery();
                String customerType = "";
                if (customerRs.next()) {
                    customerType = customerRs.getString("customer_type");
                }

                // Log kaydını ekle
                String insertLogQuery = "INSERT INTO log (logTuru, musteriTuru, urunAdi, miktar, islemTarihi, islemSonucu, customer_id) " +
                        "VALUES (?, ?, ?, ?, NOW(), ?, ?)";
                PreparedStatement insertLogStmt = conn.prepareStatement(insertLogQuery);
                insertLogStmt.setString(1, "Order Approved"); // logTuru: İşlem türü (sipariş onaylandı)
                insertLogStmt.setString(2, customerType); // musteriTuru: Müşteri türü (veritabanından alınabilir)
                insertLogStmt.setString(3, productName); // urunAdi: Ürün adı (veritabanından alınabilir)
                insertLogStmt.setInt(4, quantity); // miktar: Sipariş miktarı
                insertLogStmt.setString(5, "Order approved successfully"); // islemSonucu: İşlem sonucu
                insertLogStmt.setString(6, customerId); // customer_id: Müşteri ID'si
                insertLogStmt.executeUpdate();
            }
        } else {
            // Siparişi reddet (stok/bütçe güncelleme gerekmez)
            String updateOrderQuery = "UPDATE orders SET OrderStatus = 'Cancelled' WHERE OrderID = ?";
            PreparedStatement updateOrderStmt = conn.prepareStatement(updateOrderQuery);
            updateOrderStmt.setInt(1, orderId);
            updateOrderStmt.executeUpdate();
            
            // Log kaydetme (sipariş reddi için)
            String insertLogQuery = "INSERT INTO log (logTuru, musteriTuru, urunAdi, miktar, islemTarihi, islemSonucu, customer_id) " +
                    "VALUES (?, ?, ?, ?, NOW(), ?, ?)";
            PreparedStatement insertLogStmt = conn.prepareStatement(insertLogQuery);
            
            // Sabit değerler: Sipariş reddedildiği için bu bilgiler mevcut olmayabilir
            insertLogStmt.setString(1, "Order Rejected"); // logTuru: İşlem türü (sipariş reddedildi)
            insertLogStmt.setString(2, "Unknown"); // musteriTuru: Müşteri türü (bilinmiyor)
            insertLogStmt.setString(3, "Unknown Product"); // urunAdi: Ürün adı (bilinmiyor)
            insertLogStmt.setInt(4, 0); // miktar: 0 çünkü sipariş reddedildi
            insertLogStmt.setString(5, "Order rejected by admin"); // islemSonucu: İşlem sonucu
            insertLogStmt.setString(6, customerId); // customer_id: Bu durumda da customerId boş bırakılmamalıdır.
            insertLogStmt.executeUpdate();
        }
        
        // İşlemleri onayla
        conn.commit();
    } catch (SQLException e) {
        // Hata durumunda geri alma
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        e.printStackTrace();
    } finally {
        // Bağlantıyı kapat
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
    
    // Test etmek için örnek main metodu
    public static void main(String[] args) {
        ProductPurchaseManager manager = new ProductPurchaseManager(6);
        
        // Çoklu kullanıcı simülasyonu
        Runnable purchaseTask = () -> {
            String customerId = "customer_" + ((int) (Math.random() * 3) + 3); // customer_3, customer_4, customer_5 üretecek

            String productId = "P00" + ((int) (Math.random() * 6) + 1);
            int quantity = (int) (Math.random() * 3) + 1;
            
            boolean purchased = manager.purchaseProduct(customerId, productId, quantity);
            System.out.println("Kullanıcı " + customerId + " ürün " + productId + 
                               " satın alma durumu: " + purchased);
        };
        
        // Eş zamanlı satın alma denemeleri
        for (int i = 0; i < 10; i++) {
            new Thread(purchaseTask).start();
        }
    }
}