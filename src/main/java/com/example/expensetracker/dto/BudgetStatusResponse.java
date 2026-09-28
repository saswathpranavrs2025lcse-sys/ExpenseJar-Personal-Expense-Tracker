package com.example.expensetracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BudgetStatusResponse {

    private Long userId;
    private String userName;
    private Long categoryId;
    private String categoryName;
    private String month;
    private BigDecimal budgetAmount;
    private BigDecimal totalSpent;
    private BigDecimal remainingBudget;
    private Double percentageUsed;
    private String alertLevel;
    private String alertMessage;
    private boolean warning;

    public BudgetStatusResponse() {
    }

    public BudgetStatusResponse(Long userId, String userName, Long categoryId, String categoryName, String month, BigDecimal budgetAmount, BigDecimal totalSpent, BigDecimal remainingBudget, Double percentageUsed, String alertLevel, String alertMessage, boolean warning) {
        this.userId = userId;
        this.userName = userName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.month = month;
        this.budgetAmount = budgetAmount;
        this.totalSpent = totalSpent;
        this.remainingBudget = remainingBudget;
        this.percentageUsed = percentageUsed;
        this.alertLevel = alertLevel;
        this.alertMessage = alertMessage;
        this.warning = warning;
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

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(BigDecimal budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public BigDecimal getRemainingBudget() {
        return remainingBudget;
    }

    public void setRemainingBudget(BigDecimal remainingBudget) {
        this.remainingBudget = remainingBudget;
    }

    public BigDecimal getRemainingAmount() {
        return remainingBudget;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingBudget = remainingAmount;
    }

    public Double getPercentageUsed() {
        return percentageUsed;
    }

    public void setPercentageUsed(Double percentageUsed) {
        this.percentageUsed = percentageUsed;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public boolean isWarning() {
        return warning;
    }

    public void setWarning(boolean warning) {
        this.warning = warning;
    }

    public boolean isExceeded() {
        return "EXCEEDED".equals(alertLevel);
    }

    public void setExceeded(boolean exceeded) {
        if (exceeded) {
            this.alertLevel = "EXCEEDED";
        }
    }
}
