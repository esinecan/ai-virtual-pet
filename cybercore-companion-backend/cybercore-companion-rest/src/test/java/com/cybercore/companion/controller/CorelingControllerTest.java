package com.cybercore.companion.controller;

import com.cybercore.companion.CybercoreCompanionRestApplication;
import com.cybercore.companion.dto.CorelingStateDto;
import com.cybercore.companion.exception.ResourceNotFoundException;
import com.cybercore.companion.exception.ConcurrencyException;
import com.cybercore.companion.exception.OperationTimeoutException;
import com.cybercore.companion.service.CorelingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CorelingController.class)
@ContextConfiguration(classes = CybercoreCompanionRestApplication.class)
class CorelingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CorelingService corelingService;

    @Test
    void statusEndpointShouldReturnRunning() throws Exception {
        mockMvc.perform(get("/api/status"))
               .andExpect(status().isOk())
               .andExpect(content().string("REST API is running"));
    }

    @Test
    void getCorelingStateShouldReturnState() throws Exception {
        CorelingStateDto mockState = new CorelingStateDto(100, 50, 75, new float[]{0.1f, 0.2f});
        when(corelingService.getCorelingState(anyLong())).thenReturn(mockState);

        mockMvc.perform(get("/api/coreling/1/state"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.dataIntegrity").value(100))
               .andExpect(jsonPath("$.processingLoad").value(50))
               .andExpect(jsonPath("$.emotionalCharge").value(75))
               .andExpect(jsonPath("$.memoryVector").isArray());
    }

    @Test
    void getCorelingStateShouldReturn404WhenCorelingNotFound() throws Exception {
        when(corelingService.getCorelingState(anyLong()))
            .thenThrow(new ResourceNotFoundException("Coreling not found"));

        mockMvc.perform(get("/api/coreling/999/state"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.message").value("Coreling not found"));
    }

    @Test
    void processInteractionShouldReturnInteractionId() throws Exception {
        String mockInteractionId = "test-interaction-id";
        when(corelingService.processInteraction(anyLong(), anyString())).thenReturn(mockInteractionId);

        mockMvc.perform(post("/api/coreling/1/interact")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\": \"Hello Coreling!\"}"))
               .andExpect(status().isOk())
               .andExpect(content().string(mockInteractionId));
    }

    @Test
    void processInteractionShouldReturn400WithInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/coreling/1/interact")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"invalidField\": \"test\"}"))
               .andExpect(status().isBadRequest());
    }

    @Test
    void processInteractionShouldReturn400WithEmptyMessage() throws Exception {
        mockMvc.perform(post("/api/coreling/1/interact")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\": \"\"}"))
               .andExpect(status().isBadRequest());
    }

    @Test
    void processInteractionShouldReturn404WhenCorelingNotFound() throws Exception {
        when(corelingService.processInteraction(anyLong(), anyString()))
            .thenThrow(new ResourceNotFoundException("Coreling not found"));

        mockMvc.perform(post("/api/coreling/999/interact")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\": \"Hello Coreling!\"}"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.message").value("Coreling not found"));
    }

    @Test
    void shouldHandle409WhenOptimisticLockingFails() throws Exception {
        when(corelingService.processInteraction(anyLong(), anyString()))
            .thenThrow(new OptimisticLockingFailureException("Concurrent modification"));

        mockMvc.perform(post("/api/coreling/1/interact")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\": \"Hello Coreling!\"}"))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.message").value("Resource was updated by another request"));
    }

    @Test
    void shouldHandle409WhenLockCannotBeAcquired() throws Exception {
        when(corelingService.getCorelingState(anyLong()))
            .thenThrow(new CannotAcquireLockException("Lock acquisition failed"));

        mockMvc.perform(get("/api/coreling/1/state"))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.message").value("Resource temporarily locked"));
    }

    @Test
    void shouldHandle408WhenOperationTimesOut() throws Exception {
        when(corelingService.processInteraction(anyLong(), anyString()))
            .thenThrow(new OperationTimeoutException("Operation timed out"));

        mockMvc.perform(post("/api/coreling/1/interact")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\": \"Hello Coreling!\"}"))
               .andExpect(status().isRequestTimeout())
               .andExpect(jsonPath("$.message").value("Operation timed out"));
    }

    @Test
    void shouldHandle409OnConcurrencyConflict() throws Exception {
        when(corelingService.processInteraction(anyLong(), anyString()))
            .thenThrow(new ConcurrencyException("Concurrent interaction in progress"));

        mockMvc.perform(post("/api/coreling/1/interact")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\": \"Hello Coreling!\"}"))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.message").value("Concurrency conflict"))
               .andExpect(jsonPath("$.details").value("Concurrent interaction in progress"));
    }
}