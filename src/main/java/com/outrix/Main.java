package com.outrix;

import com.formdev.flatlaf.FlatDarkLaf;
import com.outrix.view.LoginFrame;

import javax.swing.*;

/**
 * Outrix ERP Inventory Management System
 * Entry point of the application.
 */
public class Main {

    public static void main(String[] args) {
        // Set FlatLaf Dark theme as default
        try {
            FlatDarkLaf.setup();
            UIManager.put("defaultFont", new javax.swing.plaf.FontUIResource("Segoe UI", java.awt.Font.PLAIN, 13));
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf theme: " + e.getMessage());
        }

        // System properties for better rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Launch on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
