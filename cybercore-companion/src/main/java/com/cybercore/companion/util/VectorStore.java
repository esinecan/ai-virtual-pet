package com.cybercore.companion.util;

import java.util.List;

public interface VectorStore {
    List<VectorMatch> findSimilarVectors(List<Double> queryVector, int limit);
}
