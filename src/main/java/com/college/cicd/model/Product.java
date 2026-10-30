package com.college.cicd.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Product {
    private Long id;
    private String name;
    private String sku;
    private String category;
    private Double price;
    private Integer stockQuantity;
    private String status; // "IN_STOCK", "LOW_STOCK", "OUT_OF_STOCK"
    private String description;
    private String createdAt;

    public Product() {
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public Product(Long id, String name, String sku, String category, Double price, Integer stockQuantity, String description) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.description = description;
        this.status = computeStatus(stockQuantity);
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static String computeStatus(Integer stock) {
        if (stock == null || stock <= 0) {
            return "OUT_OF_STOCK";
        } else if (stock <= 5) {
            return "LOW_STOCK";
        } else {
            return "IN_STOCK";
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { 
        this.stockQuantity = stockQuantity; 
        this.status = computeStatus(stockQuantity);
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
