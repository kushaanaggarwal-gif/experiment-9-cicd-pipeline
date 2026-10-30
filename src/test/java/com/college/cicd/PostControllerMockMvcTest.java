package com.college.cicd;

import com.college.cicd.controller.PostController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("API Integration Tests: PostController (MockMvc)")
public class PostControllerMockMvcTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PostController()).build();
    }

    @Test
    @DisplayName("API Test 1: GET /api/posts returns HTTP 200 and JSON array")
    void testGetAllPostsReturns200() throws Exception {
        mockMvc.perform(get("/api/posts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("CI/CD Best Practices"));
    }

    @Test
    @DisplayName("API Test 2: GET /api/posts/1 returns HTTP 200 with specific post")
    void testGetPostByIdReturns200() throws Exception {
        mockMvc.perform(get("/api/posts/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("CI/CD Best Practices"))
                .andExpect(jsonPath("$.author").value("DevOps Lead"));
    }

    @Test
    @DisplayName("API Test 3: GET /api/posts/999 returns HTTP 404 Not Found")
    void testGetNonExistentPostReturns404() throws Exception {
        mockMvc.perform(get("/api/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("API Test 4: POST /api/posts creates new post and returns HTTP 201")
    void testCreatePostReturns201() throws Exception {
        String newPostJson = "{\"title\":\"Continuous Deployment Strategies\",\"content\":\"Canary and blue-green releases.\",\"author\":\"Release Engineer\"}";

        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newPostJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Continuous Deployment Strategies"));
    }
}
