package com.example.frauddetector.service;

import com.example.frauddetector.repository.TransactionRepository;
import com.mongodb.client.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.bson.Document;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TransactionChangeStreamListener {
    private static final Logger log = LoggerFactory.getLogger(TransactionChangeStreamListener.class);

    private final TransactionVectorSearchService vectorSearchService;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // Keeps it synchronous
    private final MongoCollection<Document> transactionsCollection;

    public TransactionChangeStreamListener(TransactionVectorSearchService vectorSearchService, MongoCollection<Document> transactionsCollection) {
        this.vectorSearchService = vectorSearchService;
        this.transactionsCollection = transactionsCollection;
    }

    public void startListening() {
        executorService.submit(() -> {
            // Filter only to listen for INSERT Operations
            List<Bson> pipeline = List.of(Aggregates.match(Filters.eq("operationType", "insert")));

            try(MongoCursor<ChangeStreamDocument<Document>> cursor = transactionsCollection.watch(pipeline).iterator()) {
                while (cursor.hasNext()) {
                    ChangeStreamDocument<Document> change = cursor.next();
                    Document transactionDoc = change.getFullDocument();

                    if(transactionDoc != null) {
                        log.info("New transaction detected: {}", transactionDoc.getString("transactionId"));

                        List<Double> embedding = transactionDoc.getList("embedding", Double.class);

                        if(embedding != null) {
                            log.info("Performing vector search");
                            vectorSearchService.evaluateTransactionFraud(transactionDoc);
                        } else {
                            log.error("Warning: Transaction does not contain an embedding field.");
                        }
                    }
                }
            }
        });
    }
}
