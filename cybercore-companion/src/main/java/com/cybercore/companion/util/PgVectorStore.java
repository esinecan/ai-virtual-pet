package com.cybercore.companion.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PgVectorStore implements VectorStore {

    private final JdbcTemplate jdbcTemplate;

    public PgVectorStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<VectorMatch> findSimilarVectors(List<Double> queryVector, int limit) {
        String arrayString = queryVector.stream()
              .map(Object::toString)
              .collect(Collectors.joining(",", "[", "]"));

        String sql = "SELECT id, memory_vector <=> ?::vector AS similarity " +
                "FROM coreling ORDER BY similarity LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            VectorMatch match = new VectorMatch();
            match.setId(rs.getLong("id"));
            match.setSimilarity(rs.getDouble("similarity"));
            return match;
        }, arrayString, limit);
    }
}
