package com.college.cicd;

import com.college.cicd.model.Product;
import com.college.cicd.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests: ProductService Domain Logic (JUnit 5)")
public class ProductServiceTest {

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService();
    }

    @Test
    @DisplayName("Unit Test 1: Fetch seed inventory products successfully")
    void testGetAllProducts() {
        List<Product> products = productService.getAllProducts(null, null);
        assertNotNull(products);
        assertTrue(products.size() >= 5, "Seed inventory must have at least 5 products");
    }

    @Test
    @DisplayName("Unit Test 2: Create valid product and compute IN_STOCK status")
    void testCreateProductSuccess() {
        Product newProd = new Product(null, "Kubernetes Node Pool", "K8S-909", "Cloud Infrastructure", 750.0, 10, "Cluster worker node");
        Product created = productService.createProduct(newProd);

        assertNotNull(created.getId(), "Generated ID must not be null");
        assertEquals("IN_STOCK", created.getStatus(), "Stock of 10 must yield status IN_STOCK");
        assertEquals("K8S-909", created.getSku());
    }

    @Test
    @DisplayName("Unit Test 3: Create product with zero stock yields OUT_OF_STOCK")
    void testCreateProductOutOfStock() {
        Product outOfStock = new Product(null, "Solar Array Sensor", "SAS-888", "Hardware", 99.0, 0, "External light detector");
        Product created = productService.createProduct(outOfStock);

        assertEquals("OUT_OF_STOCK", created.getStatus());
    }

    @Test
    @DisplayName("Unit Test 4: Creating product with duplicate SKU throws exception")
    void testDuplicateSkuThrowsException() {
        // "GW-101" is already in seed products
        Product duplicate = new Product(null, "Fake Gateway", "GW-101", "Networking", 300.0, 5, "Duplicate");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(duplicate);
        });
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("Unit Test 5: Calculate discount percentage correctly")
    void testCalculateDiscount() {
        double discounted = productService.calculateDiscount(200.0, 15.0); // 15% off 200 = 170.0
        assertEquals(170.0, discounted, 0.001);
    }

    @Test
    @DisplayName("Unit Test 6: Calculate tax rate correctly")
    void testCalculateTax() {
        double tax = productService.calculateTax(100.0, 18.0); // 18% GST = 18.0
        assertEquals(18.0, tax, 0.001);
    }

    @Test
    @DisplayName("Unit Test 7: Verify inventory aggregation statistics")
    void testInventoryStats() {
        Map<String, Object> stats = productService.getInventoryStats();
        assertNotNull(stats);
        assertTrue((int) stats.get("totalProducts") >= 5);
        assertTrue((double) stats.get("totalInventoryValue") > 0);
    }
}
