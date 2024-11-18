package com.happy.observator.model;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class S_Order {
    
    @Id
    private String orderId;         // Order ID

    private int userId;             // User's ID
    private String action;          // "buy" or "sell"
    private String amount;          // Price for buy or volume for sell
    private String market = "KRW-BTC";

    @Column(name = "target_time")
    private LocalTime targetTime;   // Time to order

    public S_Order(){}

    public S_Order(String orderId, int userId, String action, String amount, String market, LocalTime targetTime) {
        this.orderId = orderId;
        this.userId = userId;
        this.action = action;
        this.amount = amount;
        this.market = market;
        this.targetTime = targetTime;
    }

    // Getters and setters
    public String getorderId() {return orderId;}
    public int getUserId() {return userId;}
    public String getAction() { return action; }
    public String getAmount() { return amount; }
    public String getMarket() { return market; }
    public LocalTime getTargetTime(){ return targetTime; }
}
