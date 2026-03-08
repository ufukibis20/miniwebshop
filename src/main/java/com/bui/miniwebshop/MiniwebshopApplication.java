package com.bui.miniwebshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

@SpringBootApplication
public class MiniwebshopApplication {
    public static void main(String[] args) {
        // Headless-Modus deaktivieren
        System.setProperty("java.awt.headless", "false");
        // Spring Boot Anwendung starten und ApplicationContext holen
        ConfigurableApplicationContext context = SpringApplication.run(MiniwebshopApplication.class, args);

        // WebshopApp aus dem Spring-Context holen und starten
        SwingUtilities.invokeLater(() -> {
            try {
                context.getBean(com.bui.miniwebshop.gui.WebshopApp.class).setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
