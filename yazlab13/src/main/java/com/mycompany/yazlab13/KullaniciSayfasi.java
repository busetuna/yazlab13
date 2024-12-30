/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.yazlab13;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;


public class KullaniciSayfasi extends javax.swing.JFrame {
    private static final Logger LOGGER = Logger.getLogger(KullaniciSayfasi.class.getName());
    private String currentCustomerId;
    private String currentUsername;
    public String username;
    private String currentCustomerName;
    private static final String URL = "jdbc:mysql://localhost:3306/yazlab13";
    private static final String USER = "admin";
    private static final String PASSWORD = "admin";
    
     public KullaniciSayfasi() {
        initComponents();
        getUrunlerFromDatabase();
        this.username = username; 
    
    }
       
    public KullaniciSayfasi(String username) {
        initComponents();
         this.currentUsername = username; 
        System.out.println("Debug: Constructor başladı - Username: " + username);
        getUrunlerFromDatabase();
        this.currentUsername = username;
         try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
         Statement stmt = conn.createStatement()) {
        ResultSet rs = stmt.executeQuery("SELECT * FROM app_customer");
        System.out.println("Debug: Tüm müşteriler:");
        while (rs.next()) {
            System.out.println("customer_id: " + rs.getString("customer_id") + 
                             ", customer_name: " + rs.getString("customer_name"));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    // Kullanıcı adı ile müşteri ID'sini al
        this.currentCustomerId = getCustomerIdByUsername(username);
    
       if (this.currentCustomerId != null) {
            getUrunlerFromDatabase();
            jLabel1.setText("HOŞGELDİNİZ, " + username);
        } else {
            // Müşteri ID'si alınamadıysa hata mesajı
            JOptionPane.showMessageDialog(this, 
                "Müşteri ID'si alınamadı. Lütfen kullanıcı adı ile tekrar giriş yapın.", 
                "Hata", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void getUrunlerFromDatabase() {
    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT productName, stok, price FROM app_urun")) {
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        
        while (rs.next()) {
            String productName = rs.getString("productName");
            int stok = rs.getInt("stok");
            double price = rs.getDouble("price");
            
            // Stok bilgisini ekleyerek daha detaylı bilgi göster
            String urunBilgisi = String.format("Ürün: %s | Stok: %d | Fiyat: %.2f TL", 
                productName, stok, price);
            listModel.addElement(urunBilgisi);
        }
        
        // Eğer hiç ürün yoksa kullanıcıya bilgi ver
        if (listModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Şu anda stokta ürün bulunmamaktadır.", 
                "Bilgilendirme", 
                JOptionPane.INFORMATION_MESSAGE);
        }
        
        jList1.setModel(listModel);
        
    } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, "Veritabanından ürünler çekilirken hata oluştu", e);
        JOptionPane.showMessageDialog(this, 
            "Ürünler yüklenirken bir hata oluştu: " + e.getMessage(), 
            "Veritabanı Hatası", 
            JOptionPane.ERROR_MESSAGE);
    }
}
    
    
     public void loadProducts() {
    DefaultListModel<String> model = new DefaultListModel<>(); // Yeni bir model oluştur
    String query = "SELECT productName FROM app_urun";

    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/yazlab13", "admin", "admin");
         PreparedStatement pstmt = conn.prepareStatement(query);
         ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
            String productName = rs.getString("productName");
            model.addElement(productName); // Modele ürün ekle
        }

        jList1.setModel(model); // JList'e modeli bağla

    } catch (SQLException e) {
        System.err.println("Ürünler yüklenirken hata oluştu: " + e.getMessage());
    }
}
     public String getCustomerIdByUsername(String username) {
    String customerId = null;
    String query = "SELECT customer_id FROM app_customer WHERE customer_name = ?";
    
    try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
        System.out.println("Veritabanı sorgusu çalıştırılıyor...");
        System.out.println("Username: " + username);
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    customerId = rs.getString("customer_id");
                    System.out.println("Bulunan Customer ID: " + customerId);
                } else {
                    System.out.println("Bu username için customer_id bulunamadı!");
                }
            }
        }
    } catch (SQLException e) {
        System.out.println("Veritabanı hatası: " + e.getMessage());
        LOGGER.log(Level.SEVERE, "Veritabanı hatası: ", e);
    }
    
    return customerId;
}

  
    private void setCustomerId(String username) {
    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/database_name", "root", "password");
         PreparedStatement stmt = conn.prepareStatement("SELECT customer_id FROM app_customer WHERE customer_name = ?")) {

        stmt.setString(1, username); // Kullanıcı adını SQL sorgusuna ekle
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                currentCustomerId = rs.getString("customer_id"); // Müşteri ID'sini al
                jLabel1.setText("HOŞGELDİNİZ, " + username);
            } else {
                JOptionPane.showMessageDialog(this, "Kullanıcı adı bulunamadı!");
                currentCustomerId = null; // Müşteri ID bulunamadığında null olarak ayarla
            }
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Veritabanı hatası: " + ex.getMessage());
        ex.printStackTrace();
    }
}
     
 private static String[] getCustomerList() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/yazlab13";
        String user = "admin";
        String password = "admin";
        Connection connection = DriverManager.getConnection(url, user, password);
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

             ResultSet rs = stmt.executeQuery("SELECT customer_name FROM app_customer")) {
            
            // Sonuç kümesinin boyutunu belirlemek için
            rs.last();
            int rowCount = rs.getRow();
            rs.beforeFirst();
            
            String[] customers = new String[rowCount];
            int index = 0;
            
            while (rs.next()) {
                customers[index++] = rs.getString("customer_name");
            }
            
            return customers;
            
        } catch (SQLException e) {
            Logger.getLogger(KullaniciSayfasi.class.getName())
                .log(Level.SEVERE, "listesi alınırken hata oluştu", e);
            return null;
        }
    }



    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jButton2 = new javax.swing.JButton();

        jButton1.setText("jButton1");

        jRadioButtonMenuItem1.setSelected(true);
        jRadioButtonMenuItem1.setText("jRadioButtonMenuItem1");

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane1.setBackground(new java.awt.Color(204, 255, 204));

        jList1.setBackground(new java.awt.Color(204, 255, 204));
        jScrollPane1.setViewportView(jList1);

        jButton2.setText("SATIN AL");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 431, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(66, 66, 66)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(117, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2))
                .addContainerGap(80, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
   try {
    // Kullanıcının seçtiği ürünü kontrol et
    String secilenUrun = jList1.getSelectedValue();
    if (secilenUrun == null || secilenUrun.trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Lütfen bir ürün seçin!");
        return;
    }

    // Ürün adını ayıklama ("Ürün: " kısmını kaldır ve trim ile boşlukları temizle)
    String[] parcalar = secilenUrun.split("\\|"); // "|" karakteri ile ayır
    String urunAdi = parcalar[0].replace("Ürün: ", "").trim();

    // Miktar girişini al
    String miktarStr = JOptionPane.showInputDialog(this, "Kaç adet satın almak istiyorsunuz?");
    if (miktarStr == null || miktarStr.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Geçersiz miktar!");
        
        return;
    }

    int miktar;
    try {
        miktar = Integer.parseInt(miktarStr);
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Lütfen geçerli bir sayı girin!");
        return;
    }

    // Miktar kontrolü - her üründen en fazla 5 adet alınabilir
    if (miktar > 5) {
        JOptionPane.showMessageDialog(this, "Bir üründen en fazla 5 tane alabilirsiniz.");
        return;
    }

    // Veritabanı bağlantısını oluştur
    Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/yazlab13", "admin", "admin");

    // Ürün adına göre ürün ID'sini al
    String productQuery = "SELECT urun_id, stok, price FROM app_urun WHERE productName = ?";
    PreparedStatement psProduct = conn.prepareStatement(productQuery);
    psProduct.setString(1, urunAdi); // Ürün adını sorguya ekle
    ResultSet rsProduct = psProduct.executeQuery();

    if (!rsProduct.next()) {
        JOptionPane.showMessageDialog(this, "Ürün bulunamadı!");
        return;
    }

    // Ürün ID'si, stok ve fiyat bilgisini al
    String productId = rsProduct.getString("urun_id");
    int availableStock = rsProduct.getInt("stok");
    double price = rsProduct.getDouble("price");

    // Miktar kontrolü - yeterli stok olup olmadığını kontrol et
    if (miktar > availableStock) {
        JOptionPane.showMessageDialog(this, "Stokta yeterli ürün yok!");
        return;
    }

    // Toplam fiyat hesapla
    double totalPrice = miktar * price;

    // Satın almayı veritabanına kaydet
    String updateStockQuery = "UPDATE app_urun SET stok = stok - ? WHERE urun_id = ?";
    PreparedStatement psUpdateStock = conn.prepareStatement(updateStockQuery);
    psUpdateStock.setInt(1, miktar);
    psUpdateStock.setString(2, productId);
    psUpdateStock.executeUpdate();

    // Satın alma işlemine kaydetmek için (örneğin, bir satın alma tablosuna)
    String insertPurchaseQuery = "INSERT INTO orders (customer_id, urun_id, quantity, totalPrice) VALUES (?, ?, ?, ?)";
    PreparedStatement psInsertPurchase = conn.prepareStatement(insertPurchaseQuery);
    psInsertPurchase.setString(1, currentCustomerId);
    psInsertPurchase.setString(2, productId);
    psInsertPurchase.setInt(3, miktar);
    psInsertPurchase.setDouble(4, totalPrice);
    psInsertPurchase.executeUpdate();

    // Satın alma başarı mesajı
    JOptionPane.showMessageDialog(this, "Satın alma işlemi başarılı! Toplam: " + totalPrice + " TL");

    // Müşteri premium'a geçişi kontrol et
    if (totalPrice >= 2000.0) {
        // Müşteriyi premium'a geçir
        String updateCustomerTypeQuery = "UPDATE app_customer SET customer_type = 'Premium' WHERE customer_id = ?";
        PreparedStatement psUpdateCustomerType = conn.prepareStatement(updateCustomerTypeQuery);
        psUpdateCustomerType.setString(1, currentCustomerId);
        psUpdateCustomerType.executeUpdate();

      int updatedRows = psUpdateCustomerType.executeUpdate();
if (updatedRows > 0) {
    JOptionPane.showMessageDialog(this, "Tebrikler! Premium üye oldunuz.");
} else {
    JOptionPane.showMessageDialog(this, "Müşteri tipi güncellenemedi.");
}

    }

    // Stokları güncelle
    getUrunlerFromDatabase();

} catch (SQLException e) {
    JOptionPane.showMessageDialog(this, "Bir hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
    LOGGER.log(Level.SEVERE, "Satın alma işlemi sırasında hata", e);
}



    }//GEN-LAST:event_jButton2ActionPerformed

    /**
     * @param args the command line arguments
     */public static void main(String args[]) {
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
        java.util.logging.Logger.getLogger(KullaniciSayfasi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (InstantiationException ex) {
        java.util.logging.Logger.getLogger(KullaniciSayfasi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
        java.util.logging.Logger.getLogger(KullaniciSayfasi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
        java.util.logging.Logger.getLogger(KullaniciSayfasi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    //</editor-fold>

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable() {
        public void run() {
            try {
                String[] customers = getCustomerList();

                if (customers != null && customers.length > 0) {
                    // Kullanıcı seçim dialogu
                    String selectedCustomer = (String) JOptionPane.showInputDialog(
                        null, 
                        "Kullanıcı Seçin", 
                        "Giriş", 
                        JOptionPane.QUESTION_MESSAGE, 
                        null, 
                        customers, 
                        customers[0]
                    );

                    if (selectedCustomer != null && !selectedCustomer.trim().isEmpty()) {
                        new KullaniciSayfasi(selectedCustomer).setVisible(true);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, 
                        "Kullanıcı listesi alınamadı.", 
                        "Hata", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Hata mesajını konsola yazdırın
                JOptionPane.showMessageDialog(null, 
                    "Veritabanı hatası: " + e.getMessage(), 
                    "Hata", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    });
}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JList<String> jList1;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
        }
