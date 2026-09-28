package com.example.expensetracker.dto;

import java.math.BigDecimal;

public class BudgetResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
    private String month;

    public BudgetResponse() {
    }

    public BudgetResponse(Long id, Long userId, String userName, Long categoryId, String categoryName, BigDecimal amount, String month) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.amount = amount;
        this.month = month;
    }

    public BudgetResponse(Long id, BigDecimal amount, String month, Long userId, String userName, Long categoryId, String categoryName) {
        this.id = id;
        this.amount = amount;
        this.month = month;
        this.userId = userId;
        this.userName = userName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }
}
