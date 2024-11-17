package com.happy.observator.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_asset")
public class UserAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "balance", nullable = false)
    private Double balance;

    @Column(name = "locked", nullable = false)
    private Double locked;

    @Column(name = "avg_buy_price", nullable = false)
    private Double avgBuyPrice;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Double getLocked() {
        return locked;
    }

    public void setLocked(Double locked) {
        this.locked = locked;
    }

    public Double getAvgBuyPrice() {
        return avgBuyPrice;
    }

    public void setAvgBuyPrice(Double avgBuyPrice) {
        this.avgBuyPrice = avgBuyPrice;
    }
}
