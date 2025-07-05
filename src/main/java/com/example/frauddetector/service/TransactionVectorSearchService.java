package com.example.frauddetector.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.search.SearchPath;
import com.mongodb.client.model.search.VectorSearchOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TransactionVectorSearchService {
    private static final Logger log = LoggerFactory.getLogger(TransactionChangeStreamListener.class);

    private final MongoCollection<Document> transactionCollection;
    private static final String VECTOR_INDEX_NAME = "vector_index"; //Ensure this matches vector name in Atlas
    private static final int SEARCH_LIMIT = 5; // Number of similar transactions to retrieve
    private static final int NUM_CANDIDATES = 50; // Number of approximate neighbours to consider

    public TransactionVectorSearchService(MongoCollection<Document> transactionCollection) {
        this.transactionCollection = transactionCollection;
    }

    public void evaluateTransactionFraud(Document transactionDoc) {
        String transactionId = transactionDoc.getString("transactionId");
        String userId = transactionDoc.getString("userId");
        List<Double> embedding = transactionDoc.getList("embedding", Double.class);

        // Run vector search to find similar transactions
        List<Document> similarTransactions = findSimilarTransactions(embedding, userId);

        // If no similar transaction exist for this user OR any of them are fraud -> MARK AS FRAUD
        boolean isFraud = similarTransactions.isEmpty() || similarTransactions.stream().anyMatch(doc -> doc.getBoolean("isFraud", false));

        if(isFraud) {
            markTransactionAsFraud(transactionId);
        }

    }

    List<Document> findSimilarTransactions(List<Double> embedding, String userId) {
        Bson vectorSearch = Aggregates.vectorSearch(SearchPath.fieldPath("embedding"), embedding, VECTOR_INDEX_NAME, SEARCH_LIMIT,
                VectorSearchOptions.approximateVectorSearchOptions(NUM_CANDIDATES));

        Bson matchUser = Aggregates.match(Filters.eq("userId", userId));

        return transactionCollection.aggregate(Arrays.asList(vectorSearch, matchUser)).into(new ArrayList<>());
    }

    private void markTransactionAsFraud(String transactionId) {
        transactionCollection.updateOne(Filters.eq("transactionId", transactionId), Updates.set("isFraud", true));
        log.info("Transaction marked as fraud: {}", transactionId);
    }
}
