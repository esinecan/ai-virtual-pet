package com.cybercore.companion.ai.service;

import com.cybercore.companion.ai.vector.VectorStore;
import com.cybercore.companion.ai.vector.VectorMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.kafka.core.KafkaTemplate;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LLMInteractionServiceTest {

    @Mock
    private OllamaChatModel ollamaChatClient;
    @Mock
    private EmbeddingModel embeddingModel;
    @Mock
    private VectorStore vectorStore;
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private LLMInteractionService llmService;

    @BeforeEach
    void setUp() {
        llmService = new LLMInteractionService(
            ollamaChatClient,
            embeddingModel,
            vectorStore,
            kafkaTemplate
        );
    }

    @Test
    void listenForInteraction_ShouldProcessValidMessage() {
        // Arrange
        String userId = "123";
        String userInput = "Hello AI";
        String message = userId + "|" + userInput;
        float[] mockEmbedding = {0.1f, 0.2f, 0.3f};
        List<VectorMatch> mockMatches = Collections.singletonList(
            createVectorMatch(1L, 0.95, "Previous: Hello there")
        );
        String mockResponse = "Hello! How can I help you?";

        when(embeddingModel.embed(anyString())).thenReturn(mockEmbedding);
        when(vectorStore.findSimilarVectors(any(), anyInt())).thenReturn(mockMatches);
        when(ollamaChatClient.call(anyString())).thenReturn(mockResponse);

        // Act
        llmService.listenForInteraction(message);

        // Assert
        ArgumentCaptor<List<Double>> vectorCaptor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore, times(2)).saveVector(vectorCaptor.capture(), any());
        
        List<Double> capturedVector = vectorCaptor.getAllValues().get(0);
        assertEquals(3, capturedVector.size(), "Vector should have correct dimension");
        assertEquals(0.1, capturedVector.get(0), 0.001, "First vector component should match");
        
        verify(vectorStore).findSimilarVectors(any(), eq(5));
        verify(ollamaChatClient).call(contains(userInput));
        verify(kafkaTemplate).send(
            eq("coreling.responses"), 
            eq(userId),  // Using userId as key
            eq(mockResponse)  // Response as value
        );
    }

    @Test
    void listenForInteraction_ShouldHandleInvalidMessageFormat() {
        // Arrange
        String invalidMessage = "invalid-message-format";

        // Act
        llmService.listenForInteraction(invalidMessage);

        // Assert
        verifyNoInteractions(embeddingModel, vectorStore, ollamaChatClient, kafkaTemplate);
    }

    @Test
    void listenForInteraction_ShouldHandleInvalidUserId() {
        // Arrange
        String invalidMessage = "notanumber|Hello AI";

        // Act
        llmService.listenForInteraction(invalidMessage);

        // Assert
        verifyNoInteractions(embeddingModel, vectorStore, ollamaChatClient, kafkaTemplate);
    }

    private VectorMatch createVectorMatch(Long id, double similarity, String content) {
        VectorMatch match = new VectorMatch();
        match.setId(id);
        match.setSimilarity(similarity);
        match.setContent(content);
        return match;
    }
}