package com.nexus.notification.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationRestControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void create_list_markRead_flow() throws Exception {
        // Create
        mvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "userId": "user-1",
                              "content": "Payment succeeded",
                              "type": "PAYMENT"
                            }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value("user-1"));

        // List
        mvc.perform(get("/api/notifications/user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Payment succeeded"))
                .andExpect(jsonPath("$[0].readFlag").value(false));

        // Mark as read (use id 1 assuming clean in-memory DB per test)
        mvc.perform(post("/api/notifications/1/read"))
                .andExpect(status().isNoContent());

        // Verify read
        mvc.perform(get("/api/notifications/user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].readFlag").value(true));
    }
}
