package com.example.expensetracker.dto;

import java.math.BigDecimal;

public class DashboardResponse {

    private long totalUsers;
    private long totalCategories;
    private long totalExpenses;
    private long totalBudgets;
    private BigDecimal totalSpendingAllTime;

    public DashboardResponse() {
    }

    public DashboardResponse(long totalUsers, long totalCategories, long totalExpenses, long totalBudgets, BigDecimal totalSpendingAllTime) {
        this.totalUsers = totalUsers;
        this.totalCategories = totalCategories;
        this.totalExpenses = totalExpenses;
        this.totalBudgets = totalBudgets;
        this.totalSpendingAllTime = totalSpendingAllTime;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(long totalCategories) {
        this.totalCategories = totalCategories;
    }

    public long getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(long totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public long getTotalBudgets() {
        return totalBudgets;
    }

    public void setTotalBudgets(long totalBudgets) {
        this.totalBudgets = totalBudgets;
    }

    public BigDecimal getTotalSpendingAllTime() {
        return totalSpendingAllTime;
    }

    public void setTotalSpendingAllTime(BigDecimal totalSpendingAllTime) {
        this.totalSpendingAllTime = totalSpendingAllTime;
    }
}
