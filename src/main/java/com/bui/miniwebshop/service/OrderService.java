package com.bui.miniwebshop.service;

import org.springframework.stereotype.Service;

@Service
public class OrderService {

    public String generateOrderNumber() {
        return "B-Nr." + (int)(Math.random() * 1000000);
    }

    public double applyDiscount(double total) {
        if (total > 100) {
            return total * 0.05;
        }
        return 0;
    }

}
