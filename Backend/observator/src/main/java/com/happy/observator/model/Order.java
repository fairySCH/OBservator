package com.happy.observator.model;

public class Order {
    private String action;      // "buy" or "sell"
    private String amount;      // Price for buy or volume for sell
    private String market = "KRW-BTC";

    public Order(String action, String amount) {
        this.action = action;
        this.amount = amount;
    }

    public Order(String action, String amount, String market) {
        this.action = action;
        this.amount = amount;
        this.market = market;
    }

    // Getters and setters
    public String getAction() { return action; }
    public String getAmount() { return amount; }
    public String getMarket() { return market; }
}
