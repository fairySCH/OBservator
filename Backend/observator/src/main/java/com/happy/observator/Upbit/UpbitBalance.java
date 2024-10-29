package com.happy.observator.Upbit;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpbitBalance {
    private String currency;
    private String balance;
    private String locked;
    
    @JsonProperty("avg_buy_price")
    private String avgBuyPrice;
    
    @JsonProperty("avg_buy_price_modified")
    private boolean avgBuyPriceModified;

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

    public boolean isAvgBuyPriceModified() {
        return this.avgBuyPriceModified;
    }

    public void setAvgBuyPriceModified(boolean avgBuyPriceModified) {
        this.avgBuyPriceModified = avgBuyPriceModified;
    }
}
