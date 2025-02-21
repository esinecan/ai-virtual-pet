package com.cybercore.companion.ai.vector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PgVectorStoreTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private PgVectorStore vectorStore;

    @BeforeEach
    void setUp() {
        vectorStore = new PgVectorStore(jdbcTemplate);
    }

    @Test
    void saveVector_ShouldFormatVectorAndExecuteQuery() {
        // Arrange
        List<Double> vector = Arrays.asList(0.1, 0.2, 0.3);
        String content = "test content";
        String expectedVectorString = "[0.1,0.2,0.3]";

        // Act
        vectorStore.saveVector(vector, content);

        // Assert
        verify(jdbcTemplate).update(
            eq("INSERT INTO coreling (memory_vector, text_content) VALUES (?::vector, ?)"),
            eq(expectedVectorString),
            eq(content)
        );
    }

    @Test
    void findSimilarVectors_ShouldReturnMatchingVectors() {
        // Arrange
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);
        int limit = 5;
        List<VectorMatch> expectedMatches = Arrays.asList(
            createVectorMatch(1L, 0.95, "content1"),
            createVectorMatch(2L, 0.85, "content2")
        );

        when(jdbcTemplate.query(
            anyString(),
            any(RowMapper.class),
            anyString(),
            anyInt()
        )).thenReturn(expectedMatches);

        // Act
        List<VectorMatch> result = vectorStore.findSimilarVectors(queryVector, limit);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedMatches, result);
        
        verify(jdbcTemplate).query(
            eq("SELECT id, memory_vector <=> ?::vector AS similarity, text_content FROM coreling ORDER BY similarity LIMIT ?"),
            any(RowMapper.class),
            eq("[0.1,0.2,0.3]"),
            eq(limit)
        );
    }

    private VectorMatch createVectorMatch(Long id, double similarity, String content) {
        VectorMatch match = new VectorMatch();
        match.setId(id);
        match.setSimilarity(similarity);
        match.setContent(content);
        return match;
    }
}