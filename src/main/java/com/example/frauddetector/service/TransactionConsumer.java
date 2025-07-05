package com.example.frauddetector.service;

import com.example.frauddetector.model.Transaction;
import com.example.frauddetector.repository.TransactionRepository;
import org.springframework.kafka.annotation.KafkaListener;

public class TransactionConsumer {
    private final TransactionRepository transactionRepository;

    public TransactionConsumer(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @KafkaListener(topics = "transactions", groupId = "fraud-group")
    public void consumeTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }
}
