/* package com.cybercore.companion.ai;

import com.cybercore.companion.ai.config.TestConfig;
import com.cybercore.companion.ai.config.KafkaTestConfig;
import com.cybercore.companion.ai.service.LLMInteractionService;
import com.cybercore.companion.ai.vector.VectorStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(
    classes = CybercoreCompanionLlmApplication.class,
    properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.main.allow-bean-definition-overriding=true",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration,org.springframework.ai.embedding.autoconfigure.EmbeddingAutoConfiguration,org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration,org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration"
    }
)
@Import({KafkaTestConfig.class})
@DirtiesContext
@ActiveProfiles("test")
@EmbeddedKafka(
    partitions = 1,
    count = 1,
    topics = {"coreling.interactions", "coreling.responses"},
    bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class LLMInteractionIntegrationTest {

    @MockBean
    private VectorStore vectorStore;

    @MockBean
    private OllamaChatModel ollamaChatModel;

    @MockBean
    private EmbeddingModel embeddingModel;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private LLMInteractionService llmInteractionService;

    @BeforeEach
    void setUp() {
        reset(vectorStore, ollamaChatModel, embeddingModel);
        llmInteractionService = new LLMInteractionService(
            ollamaChatModel,
            embeddingModel,
            vectorStore,
            kafkaTemplate
        );
    }

    @Test
    void testCompleteInteractionFlow() throws Exception {
        // Arrange
        String userId = "123";
        String userMessage = "Hello AI";
        float[] mockEmbedding = {0.1f, 0.2f, 0.3f};
        List<Double> expectedVector = Arrays.asList(0.1, 0.2, 0.3);
        String mockResponse = "Hello! How can I help you?";

        when(embeddingModel.embed(anyString())).thenReturn(mockEmbedding);
        when(ollamaChatModel.call(anyString())).thenReturn(mockResponse);

        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
        .thenAnswer((Answer<CompletableFuture<SendResult<String, String>>>) invocation -> 
            CompletableFuture.completedFuture(mock(SendResult.class))
        );

        // Act
        kafkaTemplate.send("coreling.interactions", userId + "|" + userMessage).get(5, TimeUnit.SECONDS);

        // Assert
        verify(embeddingModel, timeout(10000).times(2)).embed(anyString());
        verify(vectorStore, timeout(10000)).saveVector(eq(expectedVector), eq(userMessage));
        verify(vectorStore, timeout(10000)).saveVector(eq(expectedVector), eq(mockResponse));
        verify(ollamaChatModel, timeout(10000)).call(contains(userMessage));
    }
} */