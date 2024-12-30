/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.yazlab13;
import com.mycompany.yazlab13.ProductPurchaseManager;  // Eğer sınıf başka bir pakette ise

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author user
 */
public class AdminOnay extends javax.swing.JFrame {
    
    public ProductPurchaseManager purchaseManager;

    public AdminOnay() {
        initComponents();
        loadPendingOrders();
        purchaseManager = new ProductPurchaseManager(1);
    
      

    }
    private Connection getConnection(String jdbcmysqllocalhost3306yazlab13) throws SQLException {
         String DB_URL = "jdbc:mysql://localhost:3306/yazlab13";
    String USER = "admin";
    String PASS = "admin";
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

private final ReentrantLock lock = new ReentrantLock();
private Map<Integer, Long> orderProcessingTimes = new ConcurrentHashMap<>();

private void loadPendingOrders() {
    final String URL = "jdbc:mysql://localhost:3306/yazlab13";
    final String USER = "admin";
    final String PASSWORD = "admin";

    lock.lock();
    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "SELECT c.customer_id, c.customer_type, o.OrderId, o.OrderDate, o.OrderStatus " +
                        "FROM app_customer c " +
                        "JOIN orders o ON c.customer_id = o.customer_id " +
                        "WHERE o.OrderStatus = 'Pending'";

            List<Object[]> allOrders = new ArrayList<>();

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    int orderId = rs.getInt("OrderId");
                    Timestamp orderDate = rs.getTimestamp("OrderDate");
                    
                    // Eğer sipariş zaten işleniyorsa, kayıtlı bekleme süresini kullan
                    long waitingTime;
                    if (orderProcessingTimes.containsKey(orderId)) {
                        waitingTime = orderProcessingTimes.get(orderId);
                    } else {
                        // Yeni sipariş ise şu anki bekleme süresini hesapla
                        waitingTime = (System.currentTimeMillis() - orderDate.getTime()) / 1000;
                        orderProcessingTimes.put(orderId, waitingTime);
                    }

                    // Öncelik skorunu hesapla
                    String customerType = rs.getString("customer_type");
                    double priorityScore = customerType.equals("Premium") ? 
                        15 + waitingTime * 0.5 : 
                        10 + waitingTime * 0.5;

                    allOrders.add(new Object[]{
                        rs.getString("customer_id"),
                        customerType,
                        orderId,
                        waitingTime,
                        priorityScore,
                        rs.getString("OrderStatus")
                    });
                }

                // Öncelik skoruna göre sırala
                allOrders.sort((a, b) -> Double.compare((double)b[4], (double)a[4]));

                DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                model.setRowCount(0);
                for (Object[] order : allOrders) {
                    model.addRow(order);
                }
            }
        }
    } catch (ClassNotFoundException | SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Veritabanı hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
    } finally {
        lock.unlock();
    }
}
public void adminApproveOrReject(int orderId, boolean isApproved, String rejectionReason) {
    lock.lock();
    Connection conn = null;
    PreparedStatement pstmt = null;
    
    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/yazlab13", "admin", "admin");
        conn.setAutoCommit(false);
        
        // Orders ve app_customer tablolarından doğru customer_id'yi al
        String orderQuery = "SELECT o.OrderId, ac.customer_id, o.OrderDate, " +
                          "TIMESTAMPDIFF(SECOND, o.OrderDate, NOW()) as wait_time " +
                          "FROM orders o " +
                          "JOIN app_customer ac ON o.customer_id = ac.customer_id " +
                          "WHERE o.OrderId = ?";
        PreparedStatement orderStmt = conn.prepareStatement(orderQuery);
        orderStmt.setInt(1, orderId);
        ResultSet rs = orderStmt.executeQuery();
        
        if (rs.next()) {
            String customerId = rs.getString("customer_id");
            long waitingTime = rs.getLong("wait_time");
            
            // Sipariş güncelleme
            String updateQuery = "UPDATE orders SET OrderStatus = ?, rejection_reason = ? WHERE OrderId = ?";
            pstmt = conn.prepareStatement(updateQuery);
            pstmt.setString(1, isApproved ? "Approved" : "Rejected");
            pstmt.setString(2, isApproved ? null : rejectionReason);
            pstmt.setInt(3, orderId);
            pstmt.executeUpdate();

            // Log kaydı
            String logQuery = "INSERT INTO order_log (order_id, customer_id, action, action_date, waiting_time, rejection_reason) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement logStmt = conn.prepareStatement(logQuery);
            logStmt.setInt(1, orderId);
            logStmt.setString(2, customerId);
            logStmt.setString(3, isApproved ? "Approved" : "Rejected");
            logStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            logStmt.setLong(5, waitingTime);
            logStmt.setString(6, isApproved ? null : rejectionReason);
            logStmt.executeUpdate();

            conn.commit();
            
            String message = isApproved ? "Sipariş onaylandı." : "Sipariş reddedildi: " + rejectionReason;
            JOptionPane.showMessageDialog(null, message, "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            
        } else {
            throw new SQLException("Sipariş bulunamadı: " + orderId);
        }
    } catch (Exception e) {
        try {
            if (conn != null) conn.rollback();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Hata: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
    } finally {
        try {
            if (pstmt != null) pstmt.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        lock.unlock();
    }
}
private void processOrderAction(boolean isApproval) {
    lock.lock();
    try {
        // Seçili satırı kontrol et
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Lütfen bir sipariş seçin.",
                "Uyarı",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Seçili siparişin ID'sini al
        int orderId = (int) jTable1.getValueAt(selectedRow, 2);

        // İşlemi gerçekleştir
        purchaseManager.adminApproveOrReject(orderId, isApproval);

        // Tabloyu güncelle
        loadPendingOrders();

        // Kullanıcıya bilgi mesajı göster
        String message = isApproval ? "Sipariş onaylandı." : "Sipariş reddedildi.";
        JOptionPane.showMessageDialog(this,
            message,
            "Başarılı",
            JOptionPane.INFORMATION_MESSAGE);

    } finally {
        lock.unlock();
    }
}
private void displayLogsInTable() {
    try {
        // Veritabanı bağlantısını oluştur
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/yazlab13", "admin", "admin");

        // Log tablosundan tüm verileri çek
        String query = "SELECT * FROM log ORDER BY islemTarihi DESC";
        PreparedStatement ps = conn.prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        // Tablo modeli oluştur
        DefaultTableModel model = new DefaultTableModel(new String[]{"Log ID", "Log Türü", "Müşteri Türü", "Ürün Adı", "Miktar", "İşlem Tarihi", "İşlem Sonucu", "Müşteri ID"}, 0);
        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("log_id"),
                rs.getString("logTuru"),
                rs.getString("musteriTuru"),
                rs.getString("urunAdi"),
                rs.getInt("miktar"),
                rs.getTimestamp("islemTarihi").toString(),
                rs.getString("islemSonucu"),
                rs.getString("customer_id")
            });
        }

        // JTable'ı tablo modeline bağla
        jTable2.setModel(model);
        // Veritabanı bağlantısını kapat
        rs.close();
        ps.close();
        conn.close();
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Log verileri alınırken bir hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane3 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();

        jList1.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane3.setViewportView(jList1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Musteri ID", "Musteri turu", "Urun ID", "Oncelik Skoru"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jButton1.setText("ONAYLA");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("REDDET");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("SIRALA");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Musteri ID", "Musteri turu", "Urun ID", "Oncelik Skoru"
            }
        ));
        jScrollPane4.setViewportView(jTable2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                            .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 817, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(81, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addGap(18, 18, 18)
                        .addComponent(jButton2)
                        .addGap(18, 18, 18)
                        .addComponent(jButton3)
                        .addGap(37, 37, 37)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 442, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(76, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
 
    int selectedRow = jTable1.getSelectedRow();

    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, 
            "Lütfen bir sipariş seçin.", 
            "Uyarı", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    // Seçili siparişin ID'sini Integer olarak al
    int orderId = (int) jTable1.getValueAt(selectedRow, 2); // '2' sütununda OrderId var

    // Siparişi onayla
    purchaseManager.adminApproveOrReject(orderId, true);

    // Tabloyu yenile
    loadPendingOrders();  // Tablonun güncellenmesi
    displayLogsInTable();
    JOptionPane.showMessageDialog(this, 
        "Sipariş onaylandı.", 
        "Başarılı", 
        JOptionPane.INFORMATION_MESSAGE);


    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
 int selectedRow = jTable1.getSelectedRow();

    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, 
            "Lütfen bir sipariş seçin.", 
            "Uyarı", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    // Seçili siparişin ID'sini Integer olarak al
    int orderId = (int) jTable1.getValueAt(selectedRow, 2); // '2' sütununda OrderId var

    // Siparişi onayla
    purchaseManager.adminApproveOrReject(orderId, false);

    // Tabloyu yenile
    loadPendingOrders();  // Tablonun güncellenmesi
    displayLogsInTable();
    JOptionPane.showMessageDialog(this, 
        "Sipariş reddedildi.", 
        "Başarılı", 
        JOptionPane.INFORMATION_MESSAGE);

    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
displayLogsInTable();   // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AdminOnay.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AdminOnay.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AdminOnay.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdminOnay.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminOnay().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JList<String> jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    // End of variables declaration//GEN-END:variables
}
