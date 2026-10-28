package com.example.web.controller;

import com.example.web.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TestControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void successEndpointReturnsEnvelope() throws Exception {
        mockMvc.perform(get("/test/success"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data").value("ok"));
    }

    @Test
    void businessErrorMapsToNotFound() throws Exception {
        mockMvc.perform(get("/test/business-error"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("10401"));
    }

    @Test
    void redisRoundTripUsesTheContainer() throws Exception {
        mockMvc.perform(get("/test/redis").param("key", "integration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.name").value("alice"))
                .andExpect(jsonPath("$.data.age").value(18));
    }
}
