package com.example.frauddetector.service;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingGenerator {
    private final EmbeddingModel embeddingModel;

    public EmbeddingGenerator(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public float[] getEmbedding(String transaction) {
        float[] embeddingResponse = this.embeddingModel.embed(transaction);
        return embeddingResponse;
    }
}
