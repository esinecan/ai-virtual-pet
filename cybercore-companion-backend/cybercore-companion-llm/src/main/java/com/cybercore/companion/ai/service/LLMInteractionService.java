package com.cybercore.companion.ai.service;

import com.cybercore.companion.ai.vector.VectorStore;
import com.cybercore.companion.ai.vector.VectorMatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@EnableKafka
public class LLMInteractionService {

    private final OllamaChatModel ollamaChatClient;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public LLMInteractionService(
            OllamaChatModel ollamaChatClient,
            EmbeddingModel embeddingModel,
            VectorStore vectorStore,
            KafkaTemplate<String, String> kafkaTemplate) {
        this.ollamaChatClient = ollamaChatClient;
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(
        topics = "coreling.interactions",
        groupId = "${spring.kafka.consumer.group-id:llm-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenForInteraction(String message) {
        // Parse message (expected format: "userAccountId|userInput")
        String[] parts = message.split("\\|", 2);
        if (parts.length < 2) {
            log.warn("Invalid message format received: {}", message);
            return;
        }

        Long userAccountId;
        try {
            userAccountId = Long.valueOf(parts[0]);
        } catch (NumberFormatException e) {
            log.error("Invalid user account ID: {}", parts[0]);
            return;
        }
        String userInput = parts[1];

        try {
            // Generate embedding from user input
            float[] userEmbedding = embeddingModel.embed(userInput);
            List<Double> userVector = convertToDoubleList(userEmbedding);

            // Save user input embedding in VectorStore
            vectorStore.saveVector(userVector, userInput);

            // Fetch relevant stored embeddings for context
            List<VectorMatch> similarVectors = vectorStore.findSimilarVectors(userVector, 5);

            // Build prompt with RAG
            String prompt = "User input: " + userInput + "\n" +
                    "Context: " + (similarVectors.isEmpty() ? "None" : similarVectors.stream()
                            .map(VectorMatch::getContent)
                            .collect(Collectors.joining("\n")));

            // Get response from Ollama Chat Model
            String response = ollamaChatClient.call(prompt);

            // Generate embedding for LLM response
            float[] responseEmbedding = embeddingModel.embed(response);
            List<Double> responseVector = convertToDoubleList(responseEmbedding);

            // Save LLM response embedding in VectorStore
            vectorStore.saveVector(responseVector, response);

            // Send response using key-value pattern
            var sendFuture = kafkaTemplate.send("coreling.responses", userAccountId.toString(), response);
            if (sendFuture != null) {
                sendFuture.whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send response for user {}", userAccountId, ex);
                    } else {
                        log.debug("Response sent for user {}: {}", userAccountId, response);
                    }
                });
            } else {
                log.warn("KafkaTemplate.send returned null for sending response for user {}", userAccountId);
            }
        } catch (Exception e) {
            log.error("Error processing interaction for user {}", userAccountId, e);
        }
    }

    private List<Double> convertToDoubleList(float[] embedding) {
        return IntStream.range(0, embedding.length)
                .mapToObj(i -> (double) embedding[i])
                .collect(Collectors.toList());
    }
}