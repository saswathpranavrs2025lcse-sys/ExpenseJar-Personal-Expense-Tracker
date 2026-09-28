package com.example.expensetracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpenseSummaryResponse {

    private Long userId;
    private String userName;
    private Long categoryId;
    private String category;
    private String month;
    private BigDecimal totalSpent;
    private BigDecimal budget;
    private BigDecimal remaining;
    private Double percentageUsed;
    private String status; // "NORMAL", "APPROACHING", "WARNING", "EXCEEDED", "NO_BUDGET"
    private boolean warning;
    private boolean exceeded;
    private String statusMessage;

    public ExpenseSummaryResponse() {
    }

    public ExpenseSummaryResponse(Long categoryId, String category, String month, BigDecimal totalSpent, BigDecimal budget, BigDecimal remaining, Double percentageUsed, String status) {
        this.categoryId = categoryId;
        this.category = category;
        this.month = month;
        this.totalSpent = totalSpent;
        this.budget = budget;
        this.remaining = remaining;
        this.percentageUsed = percentageUsed;
        this.status = status;
        this.warning = "WARNING".equals(status) || (percentageUsed != null && percentageUsed >= 90.0);
        this.exceeded = "EXCEEDED".equals(status) || (percentageUsed != null && percentageUsed >= 100.0);
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryName() {
        return category;
    }

    public void setCategoryName(String categoryName) {
        this.category = categoryName;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public BigDecimal getBudgetAmount() {
        return budget;
    }

    public void setBudgetAmount(BigDecimal budgetAmount) {
        this.budget = budgetAmount;
    }

    public BigDecimal getRemaining() {
        return remaining;
    }

    public void setRemaining(BigDecimal remaining) {
        this.remaining = remaining;
    }

    public BigDecimal getRemainingAmount() {
        return remaining;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remaining = remainingAmount;
    }

    public Double getPercentageUsed() {
        return percentageUsed;
    }

    public void setPercentageUsed(Double percentageUsed) {
        this.percentageUsed = percentageUsed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isWarning() {
        return warning;
    }

    public void setWarning(boolean warning) {
        this.warning = warning;
    }

    public boolean isExceeded() {
        return exceeded;
    }

    public void setExceeded(boolean exceeded) {
        this.exceeded = exceeded;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
