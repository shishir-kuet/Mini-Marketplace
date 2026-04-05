package com.__2107027.mini_marketplace.controller;

import com.__2107027.mini_marketplace.dto.CategoryRequest;
import com.__2107027.mini_marketplace.dto.CategoryResponse;
import com.__2107027.mini_marketplace.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Category Controller Security Tests")
class CategoryControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @Test
    @DisplayName("POST /api/categories: unauthenticated user should get 403")
    void createCategory_unauthenticated_returnsForbidden() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics");
        request.setDescription("Devices and gadgets");

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("POST /api/categories: non-admin user should get 403")
    void createCategory_nonAdmin_returnsForbidden() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics");
        request.setDescription("Devices and gadgets");

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin1", roles = {"ADMIN"})
    @DisplayName("POST /api/categories: admin user should get 201")
    void createCategory_admin_returnsCreated() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics");
        request.setDescription("Devices and gadgets");

        CategoryResponse response = new CategoryResponse(
                1L,
                "Electronics",
                "Devices and gadgets",
                LocalDateTime.now()
        );

        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }
}
