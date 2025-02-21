package com.cybercore.companion.service;

import com.cybercore.companion.dto.CorelingStateDto;
import com.cybercore.companion.exception.ResourceNotFoundException;
import com.cybercore.companion.model.Coreling;
import com.cybercore.companion.model.Interaction;
import com.cybercore.companion.model.InteractionStatus;
import com.cybercore.companion.repository.CorelingRepository;
import com.cybercore.companion.repository.InteractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CorelingService {

    private final CorelingRepository corelingRepository;
    private final InteractionRepository interactionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public CorelingStateDto getCorelingState(Long userAccountId) {
        Coreling coreling = corelingRepository.findByUserAccountId(userAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Coreling not found"));

        return new CorelingStateDto(
            coreling.getDataIntegrity(),
            coreling.getProcessingLoad(),
            coreling.getEmotionalCharge(),
            coreling.getMemoryVector()
        );
    }

    @Transactional
    public String processInteraction(Long userAccountId, String message) {
        String interactionId = UUID.randomUUID().toString();
        
        interactionRepository.save(
            new Interaction(interactionId, userAccountId, InteractionStatus.PROCESSING)
        );

        kafkaTemplate.send(
            "coreling.interactions", 
            interactionId, 
            String.format("%s|%s", userAccountId, message)
        );

        return interactionId;
    }
}
