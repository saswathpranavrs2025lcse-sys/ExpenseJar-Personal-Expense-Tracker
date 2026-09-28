package com.example.expensetracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpendingTrendResponse {

    private Long userId;
    private String userName;
    private Long categoryId;
    private String categoryName;
    private List<MonthlySpendingItem> monthlySpending = new ArrayList<>();
    private String latestMonth;
    private String previousMonth;
    private BigDecimal monthOverMonthChange;
    private Double percentageChange;
    private String spendingTrendDirection;

    public SpendingTrendResponse() {
    }

    public SpendingTrendResponse(Long userId, String userName, List<MonthlySpendingItem> monthlySpending) {
        this.userId = userId;
        this.userName = userName;
        this.monthlySpending = monthlySpending;
    }

    public SpendingTrendResponse(Long userId, String userName, Long categoryId, String categoryName, List<MonthlySpendingItem> monthlySpending) {
        this.userId = userId;
        this.userName = userName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.monthlySpending = monthlySpending;
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

    public List<MonthlySpendingItem> getMonthlySpending() {
        return monthlySpending;
    }

    public void setMonthlySpending(List<MonthlySpendingItem> monthlySpending) {
        this.monthlySpending = monthlySpending;
    }

    public String getLatestMonth() {
        return latestMonth;
    }

    public void setLatestMonth(String latestMonth) {
        this.latestMonth = latestMonth;
    }

    public String getPreviousMonth() {
        return previousMonth;
    }

    public void setPreviousMonth(String previousMonth) {
        this.previousMonth = previousMonth;
    }

    public BigDecimal getMonthOverMonthChange() {
        return monthOverMonthChange;
    }

    public void setMonthOverMonthChange(BigDecimal monthOverMonthChange) {
        this.monthOverMonthChange = monthOverMonthChange;
    }

    public Double getPercentageChange() {
        return percentageChange;
    }

    public void setPercentageChange(Double percentageChange) {
        this.percentageChange = percentageChange;
    }

    public String getSpendingTrendDirection() {
        return spendingTrendDirection;
    }

    public void setSpendingTrendDirection(String spendingTrendDirection) {
        this.spendingTrendDirection = spendingTrendDirection;
    }
}
