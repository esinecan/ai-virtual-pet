package com.cybercore.companion.service;

import com.cybercore.companion.dto.InteractionStatusResponse;
import com.cybercore.companion.model.Coreling;
import com.cybercore.companion.model.Interaction;
import com.cybercore.companion.model.InteractionStatus;
import com.cybercore.companion.repository.CorelingRepository;
import com.cybercore.companion.repository.InteractionRepository;
import com.cybercore.companion.util.VectorStore;
import com.cybercore.companion.util.VectorMatch;
import com.cybercore.companion.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final CorelingRepository corelingRepository;
    private final VectorStore vectorStore; // Assume pgvector integration

    @KafkaListener(topics = "coreling.interactions")
    public void processInteraction(String interactionId, String payload) {
        String[] parts = payload.split("\\|");
        Long userId = Long.parseLong(parts[0]);
        String message = parts[1];

        // 1. Retrieve context using RAG
        List<Double> queryVector = getEmbedding(message); // Implement with LLM
        List<VectorMatch> matches = vectorStore.findSimilarVectors(queryVector, 5);

        // 2. Generate response using local LLM (simplified)
        String response = "Simulated LLM response based on context"; 

        // 3. Update Coreling state
        Coreling coreling = corelingRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Coreling not found"));
        
        updateCorelingState(coreling, response);

        // 4. Save interaction result
        Interaction interaction = interactionRepository.findById(UUID.fromString(interactionId))
                .orElseThrow(() -> new ResourceNotFoundException("Interaction not found"));
        
        interaction.setStatus(InteractionStatus.COMPLETED);
        interaction.setResponse(response);
        interactionRepository.save(interaction);
    }

    public InteractionStatusResponse getInteractionStatus(String interactionId) {
        Interaction interaction = interactionRepository.findById(UUID.fromString(interactionId))
                .orElseThrow(() -> new ResourceNotFoundException("Interaction not found"));

        return new InteractionStatusResponse(interaction.getStatus().name(), interaction.getResponse());
    }

    private List<Double> getEmbedding(String message) {
        // Implement embedding logic here
        return List.of();
    }

    private void updateCorelingState(Coreling coreling, String response) {
        // Simplified state update logic
        coreling.setEmotionalCharge(
            Math.min(100, coreling.getEmotionalCharge() + 10)
        );
        corelingRepository.save(coreling);
    }
}
