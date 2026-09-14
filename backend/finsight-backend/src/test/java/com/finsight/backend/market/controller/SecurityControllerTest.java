package com.finsight.backend.market.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.finsight.backend.common.ApiExceptionHandler;
import com.finsight.backend.market.entity.Security;
import com.finsight.backend.market.service.SecurityService;

@ExtendWith(MockitoExtension.class)
class SecurityControllerTest {

    @Mock
    private SecurityService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SecurityController(service))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void createsSecurityWith201() throws Exception {
        Security security = new Security("RELIANCE", "Reliance Industries", "NSE");
        when(service.create("RELIANCE", "Reliance Industries", "NSE")).thenReturn(security);

        mockMvc.perform(post("/api/securities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"RELIANCE\",\"companyName\":\"Reliance Industries\",\"exchange\":\"NSE\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.symbol").value("RELIANCE"));
    }

    @Test
    void createsExactProductionPayloadWith201() throws Exception {
        Security security = new Security("ABC", "ABC Test Company", "NSE");
        when(service.create("ABC", "ABC Test Company", "NSE")).thenReturn(security);

        mockMvc.perform(post("/api/securities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"ABC\",\"companyName\":\"ABC Test Company\",\"exchange\":\"NSE\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.symbol").value("ABC"))
                .andExpect(jsonPath("$.companyName").value("ABC Test Company"))
                .andExpect(jsonPath("$.exchange").value("NSE"));
    }

    @Test
    void rejectsInvalidSecurityInput() throws Exception {
        mockMvc.perform(post("/api/securities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"\",\"companyName\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Request validation failed"))
                .andExpect(jsonPath("$.fields.symbol").exists())
                .andExpect(jsonPath("$.fields.companyName").exists());
    }

    @Test
    void rejectsDuplicateSymbol() throws Exception {
        when(service.create(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Security symbol already exists: RELIANCE"));

        mockMvc.perform(post("/api/securities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"RELIANCE\",\"companyName\":\"Reliance Industries\",\"exchange\":\"NSE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Security symbol already exists: RELIANCE"));
    }
}