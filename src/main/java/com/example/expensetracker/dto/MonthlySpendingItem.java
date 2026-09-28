package com.example.expensetracker.dto;

import java.math.BigDecimal;

public class MonthlySpendingItem {

    private String month;
    private BigDecimal total;

    public MonthlySpendingItem() {
    }

    public MonthlySpendingItem(String month, BigDecimal total) {
        this.month = month;
        this.total = total;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getTotalSpent() {
        return total;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.total = totalSpent;
    }
}
