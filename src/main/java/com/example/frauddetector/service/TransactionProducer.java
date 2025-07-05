package com.example.frauddetector.service;

import com.example.frauddetector.model.Customer;
import com.example.frauddetector.model.Transaction;
import com.example.frauddetector.repository.CustomerRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class TransactionProducer {
    private static final Logger log = LoggerFactory.getLogger(TransactionChangeStreamListener.class);

    private static final String TOPIC = "transactions";
    private final EmbeddingGenerator embeddingGenerator;
    private final KafkaTemplate<String, Transaction> kafkaTemplate;
    private List<Customer> customers;
    private final Random random = new Random();
    private final CustomerRepository customerRepository;

    public TransactionProducer(EmbeddingGenerator embeddingGenerator, KafkaTemplate<String, Transaction> kafkaTemplate, CustomerRepository customerRepository) {
        this.embeddingGenerator = embeddingGenerator;
        this.kafkaTemplate = kafkaTemplate;
        this.customerRepository = customerRepository;
    }

    @PostConstruct
    public void loadCustomers() {
        customers = customerRepository.findAll();
        if (customers.isEmpty()) {
            log.error("Warning:No customers found! Transactions may fail");
        } else {
            log.info("Cached {} customers for transaction generation", customers.size());
        }
    }

    @Scheduled(fixedRate = 1000)
    public void generateAndSendTransactions() {
        if(customers == null ||  customers.isEmpty()) {
            log.error("No customers available. Skipping transaction generation");
            return;
        }

        Transaction transaction = Transaction.generateRandomTransaction(customers.get(random.nextInt(customers.size())));
        String embeddingText = transaction.generateEmbeddingString();
        transaction.setEmbedding(embeddingGenerator.getEmbedding(embeddingText));
        kafkaTemplate.send(TOPIC, transaction.getTransactionId(), transaction);
        log.info("Transaction sent to topic {}", TOPIC);
    }
}
