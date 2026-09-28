package com.example.expensetracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonalDashboardResponse {

    private String month;
    private BigDecimal totalSpent = BigDecimal.ZERO;
    private BigDecimal monthlyBudget = BigDecimal.ZERO;
    private BigDecimal remainingBudget = BigDecimal.ZERO;
    private Double budgetUsedPercentage = 0.0;
    private String largestCategoryName;
    private BigDecimal largestCategoryAmount = BigDecimal.ZERO;
    private String summaryText;
    private List<String> insights = new ArrayList<>();
    private List<ExpenseResponse> recentExpenses = new ArrayList<>();
    private List<ExpenseSummaryResponse> categorySummaries = new ArrayList<>();
    private List<BudgetAlertDto> budgetAlerts = new ArrayList<>();

    public PersonalDashboardResponse() {
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

    public BigDecimal getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(BigDecimal monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    public BigDecimal getRemainingBudget() {
        return remainingBudget;
    }

    public void setRemainingBudget(BigDecimal remainingBudget) {
        this.remainingBudget = remainingBudget;
    }

    public Double getBudgetUsedPercentage() {
        return budgetUsedPercentage;
    }

    public void setBudgetUsedPercentage(Double budgetUsedPercentage) {
        this.budgetUsedPercentage = budgetUsedPercentage;
    }

    public String getLargestCategoryName() {
        return largestCategoryName;
    }

    public void setLargestCategoryName(String largestCategoryName) {
        this.largestCategoryName = largestCategoryName;
    }

    public BigDecimal getLargestCategoryAmount() {
        return largestCategoryAmount;
    }

    public void setLargestCategoryAmount(BigDecimal largestCategoryAmount) {
        this.largestCategoryAmount = largestCategoryAmount;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    public String getMonthlySummary() {
        return summaryText;
    }

    public void setMonthlySummary(String monthlySummary) {
        this.summaryText = monthlySummary;
    }

    public List<String> getInsights() {
        return insights;
    }

    public void setInsights(List<String> insights) {
        this.insights = insights;
    }

    public List<ExpenseResponse> getRecentExpenses() {
        return recentExpenses;
    }

    public void setRecentExpenses(List<ExpenseResponse> recentExpenses) {
        this.recentExpenses = recentExpenses;
    }

    public List<ExpenseSummaryResponse> getCategorySummaries() {
        return categorySummaries;
    }

    public void setCategorySummaries(List<ExpenseSummaryResponse> categorySummaries) {
        this.categorySummaries = categorySummaries;
    }

    public List<BudgetAlertDto> getBudgetAlerts() {
        return budgetAlerts;
    }

    public void setBudgetAlerts(List<BudgetAlertDto> budgetAlerts) {
        this.budgetAlerts = budgetAlerts;
    }
}
