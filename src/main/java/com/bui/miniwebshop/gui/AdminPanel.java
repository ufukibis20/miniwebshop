package com.bui.miniwebshop.gui;

import com.bui.miniwebshop.model.Product;
import com.bui.miniwebshop.service.ProductService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminPanel extends JPanel {

    private final ProductService productService;
    private JTable productTable;
    private DefaultTableModel tableModel;

    public AdminPanel(ProductService productService) {
        this.productService = productService;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Tabelle
        String[] columnNames = {"Name", "Preis", "Bestand", "Kategorie"};
        tableModel = new DefaultTableModel(columnNames, 0);
        productTable = new JTable(tableModel);
        productTable.setRowHeight(24);
        productTable.setFont(new Font("Arial", Font.PLAIN, 16));

        loadProducts();

        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JButton addBtn = new JButton("➕ Produkt hinzufügen");
        JButton editBtn = new JButton("✏️ Bearbeiten");
        JButton deleteBtn = new JButton("🗑️ Löschen");

        addBtn.addActionListener(e -> addProduct());
        editBtn.addActionListener(e -> editProduct());
        deleteBtn.addActionListener(e -> deleteProduct());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadProducts() {
        tableModel.setRowCount(0); // leeren
        List<Product> products = productService.getAllProducts();
        for (Product p : products) {
            tableModel.addRow(new Object[]{p.getName(), p.getPrice(), p.getStock(), p.getCategory()});
        }
    }

    private void addProduct() {
        Product p = showProductDialog(null);
        if (p != null) {
            productService.addProduct(p);
            loadProducts();
        }
    }

    private void editProduct() {
        int row = productTable.getSelectedRow();
        if (row == -1) return;

        String name = tableModel.getValueAt(row, 0).toString();
        double price = Double.parseDouble(tableModel.getValueAt(row, 1).toString());
        int stock = Integer.parseInt(tableModel.getValueAt(row, 2).toString());
        String category = tableModel.getValueAt(row, 3).toString();

        Product edited = showProductDialog(new Product(name, price, category, stock));
        if (edited != null) {
            productService.updateProduct(edited);
            loadProducts();
        }
    }

    private void deleteProduct() {
        int row = productTable.getSelectedRow();
        if (row == -1) return;

        String name = tableModel.getValueAt(row, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Produkt \"" + name + "\" wirklich löschen?",
                "Löschen bestätigen", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            productService.deleteProductByName(name);
            loadProducts();
        }
    }

    private Product showProductDialog(Product existing) {
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField();
        JTextField categoryField = new JTextField();

        if (existing != null) {
            nameField.setText(existing.getName());
            nameField.setEditable(false); // nicht ändern bei Bearbeitung
            priceField.setText(String.valueOf(existing.getPrice()));
            stockField.setText(String.valueOf(existing.getStock()));
            categoryField.setText(existing.getCategory());
        }

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Preis:"));
        panel.add(priceField);
        panel.add(new JLabel("Bestand:"));
        panel.add(stockField);
        panel.add(new JLabel("Kategorie:"));
        panel.add(categoryField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                existing == null ? "Produkt hinzufügen" : "Produkt bearbeiten", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                int stock = Integer.parseInt(stockField.getText().trim());
                String category = categoryField.getText().trim();
                return new Product(name, price, category, stock);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ungültige Eingabe.");
            }
        }
        return null;
    }
}
