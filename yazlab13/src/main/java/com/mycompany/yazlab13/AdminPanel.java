/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.yazlab13;
import java.sql.*;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

/**
 *
 * @author user
 */
public class AdminPanel extends javax.swing.JFrame {

    private static final String URL = "jdbc:mysql://localhost:3306/yazlab13";
    private static final String USER = "admin";
    private static final String PASSWORD = "admin";
    public AdminPanel() {
        initComponents();
          
          getUrunlerFromDatabase();
    }

   // Veritabanından ürünleri çekme metodu
private void getUrunlerFromDatabase() {
    String query = "SELECT productName, stok, price FROM app_urun"; // Ürün adı, stok ve fiyat seçiliyor
    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {

        // JList için model oluşturma
        DefaultListModel<String> listModel = new DefaultListModel<>();

        // Veritabanındaki her bir ürünü JList modeline ekle
        while (rs.next()) {
            String productName = rs.getString("productName"); // Ürün adı
            int stok = rs.getInt("stok"); // Stok miktarı
            double price = rs.getDouble("price"); // Fiyat

            // Ürün bilgilerini formatlayarak modele ekle
            String urunBilgisi = String.format("Ürün: %s | Stok: %d | Fiyat: %.2f TL", productName, stok, price);
            listModel.addElement(urunBilgisi); // JList modeline ekle
        }

        // JList'e modeli atama
        jList1.setModel(listModel); // jList1 NetBeans tarafından oluşturulan JList'in adı

    } catch (SQLException e) {
        System.err.println("Veritabanından ürünler çekilirken hata oluştu: " + e.getMessage());
    }
}

private void addProduct(String urunId, String productName, int stok, double price) {
        String query = "INSERT INTO app_urun (urun_id, productName, stok, price) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, urunId);
            pstmt.setString(2, productName);
            pstmt.setInt(3, stok);
            pstmt.setDouble(4, price);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Ürün başarıyla eklendi.");
                getUrunlerFromDatabase(); // Listeyi güncelle
            } else {
                JOptionPane.showMessageDialog(this, "Ürün eklenemedi.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
        }
    }

    // Ürün silme metodu
    private void deleteProduct(String selectedProduct) {
        // Ürün adından ID'yi ve diğer bilgileri ayıkla
        String productName = selectedProduct.split("\\|")[0].replace("Ürün:", "").trim();
        String query = "DELETE FROM app_urun WHERE productName = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, productName);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Ürün başarıyla silindi.");
                getUrunlerFromDatabase(); // Listeyi güncelle
            } else {
                JOptionPane.showMessageDialog(this, "Ürün bulunamadı.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
        }
    }

    // Stok güncelleme metodu
    private void updateStock(int productId, int quantity) {
        String query = "UPDATE app_urun SET stok = stok + ? WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, quantity);
            pstmt.setString(2, String.valueOf(productId));
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Stok başarıyla güncellendi.");
                getUrunlerFromDatabase(); // Listeyi güncelle
            } else {
                JOptionPane.showMessageDialog(this, "Ürün bulunamadı.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
        }
    }



    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("ÜRÜNLER");

        jScrollPane1.setViewportView(jList1);

        jButton1.setText("ÜRÜN EKLE");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("ÜRÜN SİL");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("ÜRÜN STOK GÜNCELLE");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("ÜRÜN ONAYI");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("KAYITLAR");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 352, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(87, 87, 87)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(162, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(55, 55, 55)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addGap(38, 38, 38)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton3)
                        .addGap(36, 36, 36)
                        .addComponent(jButton4)))
                .addGap(18, 18, 18)
                .addComponent(jButton5)
                .addContainerGap(139, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    String urunId = JOptionPane.showInputDialog(this, "Ürün ID girin:");
        if (urunId == null) return;
        
        String productName = JOptionPane.showInputDialog(this, "Ürün adını girin:");
        if (productName == null) return;
        
        String stokStr = JOptionPane.showInputDialog(this, "Stok miktarını girin:");
        if (stokStr == null) return;
        
        String priceStr = JOptionPane.showInputDialog(this, "Ürün fiyatını girin:");
        if (priceStr == null) return;

        try {
            int stok = Integer.parseInt(stokStr);
            double price = Double.parseDouble(priceStr);
            addProduct(urunId, productName, stok, price);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Geçersiz sayısal değer girdiniz.");
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
     String selectedProduct = jList1.getSelectedValue();
        if (selectedProduct == null) {
            JOptionPane.showMessageDialog(this, "Lütfen silinecek ürünü seçin.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bu ürünü silmek istediğinize emin misiniz?", 
            "Silme Onayı", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            deleteProduct(selectedProduct);
        }
          
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
       String productIdStr = JOptionPane.showInputDialog(this, "Ürün ID'sini girin:");
        if (productIdStr == null) return;
        
        String quantityStr = JOptionPane.showInputDialog(this, "Eklenecek stok miktarını girin:");
        if (quantityStr == null) return;

        try {
            int productId = Integer.parseInt(productIdStr);
            int quantity = Integer.parseInt(quantityStr);
            updateStock(productId, quantity);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Geçersiz sayısal değer girdiniz.");
        
    }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        AdminOnay adminonay = new AdminOnay();
        adminonay.setVisible(true);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton5ActionPerformed

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
            java.util.logging.Logger.getLogger(AdminPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AdminPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AdminPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdminPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminPanel().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList<String> jList1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
