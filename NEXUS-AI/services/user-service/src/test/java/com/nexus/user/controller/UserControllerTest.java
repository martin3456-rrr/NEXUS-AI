package com.nexus.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.user.entity.User;
import com.nexus.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean  // Zamienione z @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldGetCurrentUserProfile() throws Exception {
        // Given
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldReturn401ForUnauthenticatedUserProfile() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldUpdateUserProfile() throws Exception {
        // Given
        User updateRequest = new User();
        updateRequest.setEmail("newemail@example.com");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("newemail@example.com");

        when(userService.updateUserProfile(eq("testuser"), any(User.class)))
                .thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())  // Poprawione z andExpected na andExpect
                .andExpect(jsonPath("$.email").value("newemail@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldGetAllUsersAsAdmin() throws Exception {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldReturn403ForNonAdminAccessingAllUsers() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldGetUserById() throws Exception {
        // Given
        when(userService.findById(1L)).thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldReturn404ForNonExistentUser() throws Exception {
        // Given
        when(userService.findById(999L)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldDeleteUserAsAdmin() throws Exception {
        // Given
        when(userService.deleteUser(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldReturn403ForNonAdminDeletingUser() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldChangePassword() throws Exception {
        // Given
        String passwordChangeRequest = """
            {
                "currentPassword": "oldpassword",
                "newPassword": "newpassword123"
            }
            """;

        when(userService.changePassword(eq("testuser"), eq("oldpassword"), eq("newpassword123")))
                .thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/users/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordChangeRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("Password changed successfully"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldReturn400ForInvalidCurrentPassword() throws Exception {
        // Given
        String passwordChangeRequest = """
            {
                "currentPassword": "wrongpassword",
                "newPassword": "newpassword123"
            }
            """;

        when(userService.changePassword(eq("testuser"), eq("wrongpassword"), eq("newpassword123")))
                .thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/users/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordChangeRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid current password"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldSearchUsers() throws Exception {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userService.searchUsers("test")).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users/search")
                        .param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldDeactivateUser() throws Exception {
        // Given
        User deactivatedUser = new User();
        deactivatedUser.setId(1L);
        deactivatedUser.setUsername("testuser");
        deactivatedUser.isEnabled();

        when(userService.deactivateUser(1L)).thenReturn(deactivatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/1/deactivate")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }
}