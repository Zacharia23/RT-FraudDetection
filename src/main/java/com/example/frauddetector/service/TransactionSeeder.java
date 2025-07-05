package com.example.frauddetector.service;

import com.example.frauddetector.model.Customer;
import com.example.frauddetector.model.Transaction;
import com.example.frauddetector.repository.CustomerRepository;
import com.example.frauddetector.repository.TransactionRepository;
import com.mongodb.client.MongoCollection;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionSeeder {
    private static final Logger log = LoggerFactory.getLogger(TransactionSeeder.class);
    private final CustomerRepository customerRepository;
    private final EmbeddingGenerator embeddingGenerator;
    private final TransactionRepository transactionRepository;
    private final TransactionChangeStreamListener transactionChangeStreamListener;
    private final MongoCollection<Document> transactionsCollection;

    public TransactionSeeder(CustomerRepository customerRepository, EmbeddingGenerator embeddingGenerator, TransactionRepository transactionRepository, TransactionChangeStreamListener transactionChangeStreamListener, MongoCollection<Document> transactionsCollection) {
        this.customerRepository = customerRepository;
        this.embeddingGenerator = embeddingGenerator;
        this.transactionRepository = transactionRepository;
        this.transactionChangeStreamListener = transactionChangeStreamListener;
        this.transactionsCollection = transactionsCollection;
    }

    @PostConstruct
    public void SeedTransactions() {
        if(transactionsCollection.countDocuments() > 0) {
            log.info("Transactions already seeded");
            return;
        }

        List<Customer> customers = customerRepository.findAll();
        List<Transaction> transactions = new ArrayList<>();

        for(Customer customer : customers) {
            for(int i = 0; i < 10; i++) {
                Transaction transaction = Transaction.generateRandomTransaction(customer);
                String embeddingText = transaction.generateEmbeddingString();
                float[] embedding = embeddingGenerator.getEmbedding(embeddingText);
                transaction.setEmbedding(embedding);
                transactions.add(transaction);
            }
        }

        transactionRepository.saveAll(transactions);
        log.info("Seeded 100 transactions.");

        transactionChangeStreamListener.startListening();
        log.info("Change Stream Listener started.");
    }
}
