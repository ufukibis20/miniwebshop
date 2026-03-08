package com.bui.miniwebshop.gui;

import com.bui.miniwebshop.model.Product;
import com.bui.miniwebshop.model.CartItem;
import com.bui.miniwebshop.service.CartService;
import com.bui.miniwebshop.service.OrderService;
import com.bui.miniwebshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import java.awt.Component;

import jakarta.annotation.PostConstruct;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.net.URL;
import java.util.List;

import static java.awt.Font.BOLD;

@org.springframework.stereotype.Component
public class WebshopApp extends JFrame {

    private DefaultTableModel tableModel;
    private JTable productTable;
    private JTextField searchField;
    private List<Product> allProducts;

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    public WebshopApp() throws UnsupportedLookAndFeelException {
        // Eigenschaften des Fensters
        setTitle("Ufuk's Webshop");
        setBackground(new Color(231, 254, 255));
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    // Klasse zum filtern der Produkte (für Suchoption)
    private void filterProducts() {
        String keyword = searchField.getText().toLowerCase();
        tableModel.setRowCount(0); // Tabelle leeren

        for (Product p : productService.getAllProducts()) {
            if (p.getName().toLowerCase().contains(keyword)) {
                tableModel.addRow(new Object[]{
                        p.getName(), p.getPrice(), p.getStock()
                });
            }
        }
    }

    @PostConstruct
    private void initComponents() {
        JTabbedPane tabs = new JTabbedPane();

        // === Shop-Tab ===
        JPanel shopPanel = new JPanel(new BorderLayout());

        String[] columnNames = {"Produkt", "Preis"};
        tableModel = new DefaultTableModel(columnNames, 0);
        productTable = new JTable(tableModel);

        // Suchfeld
        searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 16));
        searchField.setPreferredSize(new Dimension(300, 30));
        searchField.setToolTipText("Produkte durchsuchen");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterProducts(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterProducts(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterProducts(); }
        });

        // Produkte laden
        allProducts = productService.getAllProducts();
        for (Product p : allProducts) {
            tableModel.addRow(new Object[]{p.getName(), p.getPrice()});
        }

        // Tabellenstyling
        productTable.setFont(new Font("Arial", Font.PLAIN, 18));
        productTable.setRowHeight(28);
        productTable.getTableHeader().setFont(new Font("Arial", BOLD, 22));
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setDefaultRenderer(Object.class, new AlternateRowColorRenderer());

        // Buttons
        JButton addToCartButton = new JButton("🛒 In den Warenkorb");
        JButton showCartButton = new JButton("📦 Warenkorb anzeigen");
        JButton placeOrderButton = new JButton("✅ Bestellung abschließen");

        customizeButton(addToCartButton);
        customizeButton(showCartButton);
        customizeButton(placeOrderButton);

        addToCartButton.addActionListener(e -> addToCart());
        showCartButton.addActionListener(e -> showCart());
        placeOrderButton.addActionListener(e -> placeOrder());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(addToCartButton);
        buttonPanel.add(showCartButton);
        buttonPanel.add(placeOrderButton);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        searchPanel.add(new JLabel("🔍 Suche:"));
        searchPanel.add(searchField);

        shopPanel.add(searchPanel, BorderLayout.NORTH);
        shopPanel.add(new JScrollPane(productTable), BorderLayout.CENTER);
        shopPanel.add(buttonPanel, BorderLayout.SOUTH);

        tabs.add("🛍 Shop", shopPanel);

        // === Admin-Tab ===
        JPanel adminPanel = new AdminPanel(productService);
        tabs.add("🛠 Admin-Bereich", adminPanel);

        add(tabs, BorderLayout.CENTER);
        setVisible(true);
    }


    // Aussehen der Buttons
    private void customizeButton(JButton button) {
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(200, 60));
        button.setForeground(Color.BLACK);
        button.setBackground(new Color(231, 254, 255)); // Microsoft-Blau
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setOpaque(true);

        // Hover-Effekt
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(146, 239, 243));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(231, 254, 255));
            }
        });
    }



    // Methode zum Einfügen in den Warenkorb
    private void addToCart() {
        int viewIndex = productTable.getSelectedRow();
        if (viewIndex != -1) {
            int modelIndex = productTable.convertRowIndexToModel(viewIndex);
            Product selectedProduct = allProducts.get(modelIndex);

            // Prüfen ob Produkt vorhanden ist
            if (selectedProduct.getStock() == 0) {
                playSound("error.wav");
                JOptionPane.showMessageDialog(this, "Dieses Produkt ist leider ausverkauft und kann nicht in den Warenkorb gelegt werden.", "Nicht verfügbar", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String input = JOptionPane.showInputDialog(this, "Menge:");
            try {
                int quantity = Integer.parseInt(input);
                if (quantity > 0) {
                    int available = selectedProduct.getStock();
                    if (quantity > available) {     // check ob produkt vorhanden ist bzw. genug bestand da ist
                        JOptionPane.showMessageDialog(this, "Nur " + available + " Stück verfügbar!");
                        return;
                    }
                    cartService.addProduct(selectedProduct, quantity);
                    JOptionPane.showMessageDialog(this, "Produkt hinzugefügt!");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ungültige Eingabe.");
            }
        }
    }

    // Methode zum Anzeigen des Warenkorbs
    private void showCart() {
        List<CartItem> items = cartService.getItems();
        StringBuilder sb = new StringBuilder();
        sb.append("Warenkorb:\n");

        for (CartItem item : items) {
            sb.append(item.getQuantity()).append("x ")
                    .append(item.getProduct().getName()).append(" - ")
                    .append(String.format("%.2f €", item.getProduct().getPrice()))
                    .append("\n");
        }

        sb.append("-------------------------\n");
        double total = cartService.calculateTotal();
        sb.append("Gesamt: ").append(String.format("%.2f €", total)).append("\n");

        JOptionPane.showMessageDialog(this, sb.toString());
    }

    // Methode um Bestellung abzuschließen
    private void placeOrder() {
        double total = cartService.calculateTotal();
        double discount = orderService.applyDiscount(total);
        double finalTotal = total - discount;
        String orderNumber = orderService.generateOrderNumber();

        JDialog loadingDialog = new JDialog(this, "Bestellung wird verarbeitet...", true);
        JLabel loadingLabel = new JLabel("⏳ Bitte warten...");
        loadingLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadingDialog.add(loadingLabel);
        loadingDialog.setSize(300, 100);
        loadingDialog.setLocationRelativeTo(this);

// Extra Thread zum Bestellen (damit GUI nicht einfriert)
        new Thread(() -> {
            try {
                Thread.sleep(1500); // Simulierter Ladevorgang (1,5 Sek.)

                for (CartItem item : cartService.getItems()) {
                    Product product = item.getProduct();
                    product.reduceStock(item.getQuantity());

                    // Neuen Bestand in Datenbank speichern
                    productService.updateStock(product.getName(), product.getStock());
                }

                cartService.clear();

                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose(); // Lade-Dialog schließen

                    // Erfolgs-Popup anzeigen
                    JOptionPane.showMessageDialog(this,
                            "Vielen Dank für Ihre Bestellung!\nBestellnummer: " + orderNumber +
                                    "\nZu zahlen: " + String.format("%.2f €", finalTotal),
                            "Erfolg",
                            JOptionPane.INFORMATION_MESSAGE);

                    playSound("success.wav");

                    filterProducts(); // Tabelle aktualisieren
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                loadingDialog.dispose();
            }
        }).start();

        loadingDialog.setVisible(true); // Zeige das Fenster (blockiert Main-Thread bis dispose)


        StringBuilder sb = new StringBuilder();
        sb.append("Bestellung abgeschlossen!\n");
        sb.append("Bestellnummer: ").append(orderNumber).append("\n");
        sb.append("Gesamt: ").append(String.format("%.2f €", total)).append("\n");
        if (discount > 0) {
            sb.append("Rabatt: -").append(String.format("%.2f €", discount)).append("\n");
        }
        sb.append("Zu zahlen: ").append(String.format("%.2f €", finalTotal)).append("\n");

        JOptionPane.showMessageDialog(this, sb.toString());

        // Bestand anpassen (nach einer Bestellung)
        for (CartItem item: cartService.getItems()) {
            item.getProduct().reduceStock(item.getQuantity());
        }
        cartService.clear();
        filterProducts();
    }

    // Mouseover-Effekt für Produkt Tabelle

    private static int hoveredRow = -1; // Für Mouseover-Zeile


    // Klasse für abwechselnde Zeilenfarben
    // Benutzerdefinierter Renderer für abwechselnde Zeilenfarben
    private class AlternateRowColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Product product = allProducts.get(table.convertRowIndexToModel(row));
            boolean isOutOfStock = product.getStock() == 0;

            // Preis-Spalte formatieren (Spalte 1)
            if (column == 1 && value instanceof Number) {
                double price = ((Number) value).doubleValue();
                setText(String.format("%.2f €", price));
            }

            if (column == 2) {
                if (value instanceof Number && ((Number) value).intValue() == 0) {
                    setText("Leider ausverkauft");
                    setForeground(Color.red);
                } else if (value instanceof Number) {
                    setText(String.valueOf(((Number) value).intValue()));
                    setForeground(Color.BLACK);
                }
            }

            // Standard-Stil
            setFont(new Font("Arial", Font.PLAIN, 18));
            setHorizontalAlignment(SwingConstants.LEFT);
            setForeground(Color.BLACK);

            // Farben
            if (isSelected) {
                c.setBackground(new Color(146, 239, 243));
            } else if (isOutOfStock) {
                setBackground(new Color(135,22,20));
                setForeground(Color.white);
            } else if (row == hoveredRow) {
                c.setBackground(new Color(231, 254, 255));
            } else if (row % 2 == 0) {
                c.setBackground(new Color(245, 245, 245));
            } else {
                c.setBackground(Color.WHITE);
            }

            return c;
        }
    }

    // Soundeffekte
    private void playSound(String fileName) {
        try {
            URL soundURL = getClass().getClassLoader().getResource("sounds/" + fileName);
            if (soundURL == null) {
                System.err.println("Sounddatei nicht gefunden: " + fileName);
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            System.err.println("Fehler beim Abspielen von " + fileName + ": " + e.getMessage());
        }
    }



}