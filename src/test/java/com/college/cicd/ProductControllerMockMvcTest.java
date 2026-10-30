package com.college.cicd;

import com.college.cicd.controller.ProductController;
import com.college.cicd.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("API Integration Tests: ProductController (MockMvc)")
public class ProductControllerMockMvcTest {

    private MockMvc mockMvc;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService();
        ProductController controller = new ProductController();
        ReflectionTestUtils.setField(controller, "productService", productService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("API Test 1: GET /api/products returns HTTP 200 and product list")
    void testGetProductsReturns200() throws Exception {
        mockMvc.perform(get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].sku").exists());
    }

    @Test
    @DisplayName("API Test 2: GET /api/products/1 returns HTTP 200 with product details")
    void testGetProductByIdReturns200() throws Exception {
        mockMvc.perform(get("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Enterprise Cloud Gateway"))
                .andExpect(jsonPath("$.sku").value("GW-101"));
    }

    @Test
    @DisplayName("API Test 3: POST /api/products creates product and returns HTTP 201")
    void testCreateProductReturns201() throws Exception {
        String newProductJson = "{" +
            "\"name\":\"High-Speed Fiber Switch\"," +
            "\"sku\":\"FS-777\"," +
            "\"category\":\"Networking\"," +
            "\"price\":599.0," +
            "\"stockQuantity\":8," +
            "\"description\":\"100GbE managed rack switch\"" +
            "}";

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newProductJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("High-Speed Fiber Switch"))
                .andExpect(jsonPath("$.status").value("IN_STOCK"));
    }

    @Test
    @DisplayName("API Test 4: POST /api/products with invalid data returns HTTP 400 Bad Request")
    void testCreateInvalidProductReturns400() throws Exception {
        String invalidJson = "{\"name\":\"\",\"sku\":\"\",\"price\":-10.0}";

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("API Test 5: DELETE /api/products/1 returns HTTP 204 No Content")
    void testDeleteProductReturns204() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("API Test 6: GET /api/products/stats returns HTTP 200 with inventory counts")
    void testGetStatsReturns200() throws Exception {
        mockMvc.perform(get("/api/products/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").isNumber())
                .andExpect(jsonPath("$.totalInventoryValue").isNumber());
    }
}
