package com.happy.observator.model;

public class OrderRequest {
    private int userId;          // user id
    private String action;       // buy or sell
    private String amount;       // Price or value depending on action
    private String execute_time;   // In HH:mm:ss.SSS format

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAmount() {
        return this.amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getExecute_time() {
        return this.execute_time;
    }

    public void setTargetTime(String execute_time) {
        this.execute_time = execute_time;
    }
}