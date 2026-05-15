package com.finance;

import com.finance.service.FinanceService;
import com.finance.ui.MainFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Enable hardware acceleration
        System.setProperty("sun.java2d.opengl", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                // Use the system look and feel as a base
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

                // Override key defaults for our dark theme
                UIManager.put("OptionPane.background",         new java.awt.Color(0x1A, 0x21, 0x35));
                UIManager.put("Panel.background",              new java.awt.Color(0x1A, 0x21, 0x35));
                UIManager.put("OptionPane.messageForeground",  new java.awt.Color(0xF0, 0xF4, 0xFF));
                UIManager.put("Button.background",             new java.awt.Color(0x2A, 0x35, 0x50));
                UIManager.put("Button.foreground",             new java.awt.Color(0xF0, 0xF4, 0xFF));
                UIManager.put("TextField.background",          new java.awt.Color(0x1E, 0x27, 0x3E));
                UIManager.put("TextField.foreground",          new java.awt.Color(0xF0, 0xF4, 0xFF));
                UIManager.put("TextField.caretForeground",     new java.awt.Color(0xF0, 0xF4, 0xFF));
                UIManager.put("ComboBox.background",           new java.awt.Color(0x1E, 0x27, 0x3E));
                UIManager.put("ComboBox.foreground",           new java.awt.Color(0xF0, 0xF4, 0xFF));
                UIManager.put("ScrollBar.thumb",               new java.awt.Color(0x2A, 0x35, 0x50));
                UIManager.put("ScrollBar.track",               new java.awt.Color(0x1A, 0x21, 0x35));
                UIManager.put("Dialog.background",             new java.awt.Color(0x1A, 0x21, 0x35));

            } catch (Exception e) {
                e.printStackTrace();
            }

            FinanceService service = new FinanceService();
            new MainFrame(service);
        });
    }
}
