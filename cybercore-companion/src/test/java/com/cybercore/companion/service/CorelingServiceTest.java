package com.cybercore.companion.service;

import com.cybercore.companion.model.Coreling;
import com.cybercore.companion.repository.CorelingRepository;
import com.cybercore.companion.repository.InteractionRepository;
import com.cybercore.companion.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorelingServiceTest {

    @Mock
    private CorelingRepository corelingRepository;
    
    @Mock
    private InteractionRepository interactionRepository;
    
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @InjectMocks
    private CorelingService corelingService;

    @Test
    void getCorelingState_NotFound() {
        when(corelingRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () ->
            corelingService.getCorelingState(1L)
        );
    }

    @Test
    void processInteraction_SavesAndPublishes() {
        when(interactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        
        String interactionId = corelingService.processInteraction(1L, "Hello");
        
        assertNotNull(interactionId);
        verify(kafkaTemplate).send(eq("coreling.interactions"), anyString(), anyString());
    }
}
