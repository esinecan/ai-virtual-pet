package com.cybercore.companion.service;

import com.cybercore.companion.model.Coreling;
import com.cybercore.companion.model.Interaction;
import com.cybercore.companion.repository.CorelingRepository;
import com.cybercore.companion.repository.InteractionRepository;
import com.cybercore.companion.util.VectorStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InteractionServiceTest {

    @Mock
    private InteractionRepository interactionRepository;
    
    @Mock
    private CorelingRepository corelingRepository;
    
    @Mock
    private VectorStore vectorStore;
    
    @InjectMocks
    private InteractionService interactionService;

    @Test
    void processInteraction_UpdatesState() {
        // Given
        Coreling coreling = new Coreling();
        coreling.setEmotionalCharge(50);
        UUID interactionId = UUID.randomUUID();

        when(corelingRepository.findByUserAccountId(anyLong())).thenReturn(Optional.of(coreling));
        when(vectorStore.findSimilarVectors(any(), anyInt())).thenReturn(List.of());
        when(interactionRepository.findById(interactionId)).thenReturn(Optional.of(new Interaction()));

        // When
        interactionService.processInteraction(interactionId.toString(), "1|Hello");

        // Then
        assertEquals(60, coreling.getEmotionalCharge());
        verify(interactionRepository).save(any());
    }
}
