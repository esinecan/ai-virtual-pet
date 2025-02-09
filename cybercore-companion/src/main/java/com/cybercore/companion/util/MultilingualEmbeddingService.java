package com.cybercore.companion.util;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MultilingualEmbeddingService {

    private static final String MODEL_NAME = "sentence-transformers/paraphrase-multilingual-mpnet-base-v2";
    private HuggingFaceTokenizer tokenizer;

    @PostConstruct
    public void init() throws Exception {
        this.tokenizer = HuggingFaceTokenizer.newInstance(MODEL_NAME);
    }

    public List<Float> generateEmbedding(String text) {
        try {
            HuggingFaceTokenizer.BatchEncoding encoding = tokenizer.batchEncode(List.of(text));
            NDArray output = tokenizer.getModel().predict(new NDList(encoding.getTokenIds(), encoding.getAttentionMask())).get(0);
            return output.toFloatArray().boxed().collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Embedding generation failed", e);
        }
    }
}
