package com.college.cicd.service;

import com.college.cicd.model.Product;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final Map<Long, Product> productStore = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public ProductService() {
        // Initialize default seed products for practical demonstration
        saveSeedProduct("Enterprise Cloud Gateway", "GW-101", "Networking", 499.99, 12, "High-throughput API gateway appliance");
        saveSeedProduct("Microservice Mesh Node", "MS-202", "Cloud Tools", 249.50, 4, "Zero-trust service mesh sidecar manager");
        saveSeedProduct("Edge Sensor Hub v2", "ES-303", "IoT Devices", 129.00, 0, "Ultra low-power industrial sensor collector");
        saveSeedProduct("Kubernetes Storage Controller", "SC-404", "Storage", 899.00, 18, "Persistent container volume orchestrator");
        saveSeedProduct("DevOps CI Pipeline Runner", "CR-505", "DevOps Tools", 349.00, 2, "Hardware-accelerated continuous integration runner");
    }

    private void saveSeedProduct(String name, String sku, String category, double price, int stock, String description) {
        Long id = idSequence.getAndIncrement();
        Product product = new Product(id, name, sku, category, price, stock, description);
        productStore.put(id, product);
    }

    public List<Product> getAllProducts(String search, String category) {
        return productStore.values().stream()
            .filter(p -> category == null || category.trim().isEmpty() || p.getCategory().equalsIgnoreCase(category.trim()))
            .filter(p -> search == null || search.trim().isEmpty() || 
                         p.getName().toLowerCase().contains(search.toLowerCase()) || 
                         p.getSku().toLowerCase().contains(search.toLowerCase()))
            .sorted(Comparator.comparing(Product::getId))
            .collect(Collectors.toList());
    }

    public Optional<Product> getProductById(Long id) {
        return Optional.ofNullable(productStore.get(id));
    }

    public Product createProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (product.getPrice() == null || product.getPrice() < 0) {
            throw new IllegalArgumentException("Product price must be non-negative");
        }
        if (product.getSku() == null || product.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("Product SKU is mandatory");
        }

        // Check SKU uniqueness
        boolean skuExists = productStore.values().stream()
            .anyMatch(p -> p.getSku().equalsIgnoreCase(product.getSku().trim()));
        if (skuExists) {
            throw new IllegalArgumentException("Product with SKU '" + product.getSku() + "' already exists");
        }

        Long id = idSequence.getAndIncrement();
        product.setId(id);
        product.setSku(product.getSku().trim().toUpperCase());
        product.setStatus(Product.computeStatus(product.getStockQuantity()));
        productStore.put(id, product);
        return product;
    }

    public Optional<Product> updateProduct(Long id, Product updated) {
        if (!productStore.containsKey(id)) {
            return Optional.empty();
        }
        Product existing = productStore.get(id);
        existing.setName(updated.getName());
        existing.setCategory(updated.getCategory());
        existing.setPrice(updated.getPrice());
        existing.setStockQuantity(updated.getStockQuantity());
        existing.setDescription(updated.getDescription());
        existing.setStatus(Product.computeStatus(updated.getStockQuantity()));
        return Optional.of(existing);
    }

    public boolean deleteProduct(Long id) {
        return productStore.remove(id) != null;
    }

    public double calculateDiscount(double price, double discountPercent) {
        if (price < 0 || discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("Invalid price or discount percentage");
        }
        double discountAmount = (price * discountPercent) / 100.0;
        return Math.round((price - discountAmount) * 100.0) / 100.0;
    }

    public double calculateTax(double price, double taxRatePercent) {
        if (price < 0 || taxRatePercent < 0) {
            throw new IllegalArgumentException("Invalid price or tax rate");
        }
        double taxAmount = (price * taxRatePercent) / 100.0;
        return Math.round(taxAmount * 100.0) / 100.0;
    }

    public Map<String, Object> getInventoryStats() {
        int totalProducts = productStore.size();
        long inStock = productStore.values().stream().filter(p -> "IN_STOCK".equals(p.getStatus())).count();
        long lowStock = productStore.values().stream().filter(p -> "LOW_STOCK".equals(p.getStatus())).count();
        long outOfStock = productStore.values().stream().filter(p -> "OUT_OF_STOCK".equals(p.getStatus())).count();
        double totalValue = productStore.values().stream()
            .mapToDouble(p -> p.getPrice() * (p.getStockQuantity() != null ? p.getStockQuantity() : 0))
            .sum();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalProducts", totalProducts);
        stats.put("inStockCount", inStock);
        stats.put("lowStockCount", lowStock);
        stats.put("outOfStockCount", outOfStock);
        stats.put("totalInventoryValue", Math.round(totalValue * 100.0) / 100.0);
        return stats;
    }
}
