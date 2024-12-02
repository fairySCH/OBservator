package com.happy.observator.Upbit;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpbitBalance {
    private String currency;
    private String balance;
    private String locked;
    
    @JsonProperty("avg_buy_price")
    private String avgBuyPrice;

    @Override
    public String toString() {
        return "UpbitBalance{" +
                "currency='" + currency + '\'' +
                ", balance=" + balance +
                ", avgBuyPrice=" + avgBuyPrice +
                '}';
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getBalance() {
        return this.balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getLocked() {
        return this.locked;
    }

    public void setLocked(String locked) {
        this.locked = locked;
    }

    public String getAvgBuyPrice() {
        return this.avgBuyPrice;
    }

    public void setAvgBuyPrice(String avgBuyPrice) {
        this.avgBuyPrice = avgBuyPrice;
    }
}
