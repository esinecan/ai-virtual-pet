package com.cybercore.companion.ai.vector;

import java.util.List;

public interface VectorStore {
    List<VectorMatch> findSimilarVectors(List<Double> queryVector, int limit);
    void saveVector(List<Double> vector, String content);
}
