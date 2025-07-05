package com.example.frauddetector.model;

import com.example.frauddetector.enums.Category;
import com.example.frauddetector.enums.Currency;
import com.example.frauddetector.enums.Merchant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Random;

@Document(collation = "customers")
public class Customer {
    @Id
    private String id;
    private final String userId;
    private final List<Merchant> merchants;
    private final List<Category> categories;
    private final Double meanSpending;
    private final Double spendingStdDev;
    private final Currency preferredCurrency;

    public Customer(String userId, List<Merchant> merchants, List<Category> categories, Double meanSpending, Double spendingStdDev, Currency preferredCurrency) {
        this.userId = userId;
        this.merchants = merchants;
        this.categories = categories;
        this.meanSpending = meanSpending;
        this.spendingStdDev = spendingStdDev;
        this.preferredCurrency = preferredCurrency;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public List<Merchant> getMerchants() {
        return merchants;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public Double getMeanSpending() {
        return meanSpending;
    }

    public Double getSpendingStdDev() {
        return spendingStdDev;
    }

    public Currency getPreferredCurrency() {
        return preferredCurrency;
    }

    public Category getFrequentCategory() {
        Random random = new Random();
        return categories.get(random.nextInt(categories.size()));
    }

    public Category getNotFrequentCategory() {
        List<Category> allCategories = List.of(Category.values());

        List<Category> infrequentCategories = allCategories.stream()
                .filter(category -> !categories.contains(category))
                .toList();

        Random random = new Random();

        return infrequentCategories.get(random.nextInt(infrequentCategories.size()));
    }

    public Currency getRandomSuspiciousCurrency() {
        List<Currency> allCurrencies = List.of(Currency.values());

        List<Currency> infrequentCurrencies = allCurrencies.stream()
                .filter(currency -> !categories.contains(currency))
                .toList();

        Random random = new Random();
        return infrequentCurrencies.get(random.nextInt(infrequentCurrencies.size()));
    }
}
