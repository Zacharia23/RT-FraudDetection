package com.example.frauddetector.repository;

import com.example.frauddetector.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.CrudRepository;

public interface CustomerRepository extends MongoRepository<Customer, String> {
}
