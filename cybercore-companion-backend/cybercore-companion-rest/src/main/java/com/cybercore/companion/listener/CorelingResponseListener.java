package com.cybercore.companion.listener;

import com.cybercore.companion.service.CorelingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CorelingResponseListener {

    private final CorelingService corelingService;

    @KafkaListener(
        topics = "coreling.responses",
        groupId = "${spring.kafka.consumer.group-id:coreling-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleResponse(String userAccountId, String responseMessage) {
        try {
            Long accountId = Long.valueOf(userAccountId);
            log.debug("Received response for user {}: {}", accountId, responseMessage);
            //TODO: corelingService.handleLLMResponse(accountId, responseMessage);
            // 1. Update interaction status in database
            // 2. Send response via WebSocket to connected clients
            // 3. Store the response in interaction history
        } catch (NumberFormatException ex) {
            log.error("Invalid user account ID received: {}", userAccountId, ex);
        } catch (Exception ex) {
            log.error("Error processing LLM response for user {}", userAccountId, ex);
        }
    }
}