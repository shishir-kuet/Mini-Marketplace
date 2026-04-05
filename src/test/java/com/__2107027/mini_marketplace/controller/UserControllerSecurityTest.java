package com.__2107027.mini_marketplace.controller;

import com.__2107027.mini_marketplace.dto.UserResponse;
import com.__2107027.mini_marketplace.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("User Controller Security Tests")
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("GET /api/users: non-admin user should get 403")
    void getAllUsers_nonAdmin_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin1", roles = {"ADMIN"})
    @DisplayName("GET /api/users: admin user should get 200")
    void getAllUsers_admin_returnsOk() throws Exception {
        UserResponse user = new UserResponse(
                1L,
                "demo-user",
                "demo@example.com",
                "user",
                LocalDateTime.now()
        );

        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].username").value("demo-user"));
    }
}
