package com.bui.miniwebshop.service;

import com.bui.miniwebshop.model.CartItem;
import com.bui.miniwebshop.model.Product;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service // <- Damit Spring die Klasse verwalten kann
public class CartService {
    private List<CartItem> items = new ArrayList<>();

    public void addProduct(Product product, int quantity) {
        items.add(new CartItem(product, quantity));
    }

    public List<CartItem> getItems() {
        return items;
    }

    public double calculateTotal() {
        return items.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }

    public void clear() {
        items.clear();
    }
}