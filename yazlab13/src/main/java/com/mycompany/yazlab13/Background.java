/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.yazlab13;

import java.awt.BorderLayout;
import java.awt.Graphics;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Background extends JFrame {
    public Background() {
        // Özel bir JPanel oluştur
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Resmi yükle
                ImageIcon backgroundImage = new ImageIcon("src1/img/Adsız tasarım (1).png");
                g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };

        // JPanel özelliklerini ayarla
        backgroundPanel.setLayout(new BorderLayout());
        
        // JPanel'i JFrame'e ekle
        this.add(backgroundPanel);

        // JFrame özelliklerini ayarla
        setTitle("Arka Plan Resmi Örneği");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}

