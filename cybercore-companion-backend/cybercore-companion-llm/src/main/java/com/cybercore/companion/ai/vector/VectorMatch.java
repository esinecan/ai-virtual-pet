package com.cybercore.companion.ai.vector;

import lombok.Data;

@Data
public class VectorMatch {
    private Long id;
    private double similarity;
    private String content;
}
