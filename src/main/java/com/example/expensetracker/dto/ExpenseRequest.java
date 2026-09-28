package com.example.expensetracker.dto;

import com.example.expensetracker.entity.RecurringType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseRequest {

    private Long userId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Expense amount is required")
    @Positive(message = "Expense amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Expense date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String description;

    private RecurringType recurringType = RecurringType.ONE_TIME;

    public ExpenseRequest() {
    }

    public ExpenseRequest(Long userId, Long categoryId, BigDecimal amount, LocalDate date, String description) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.recurringType = RecurringType.ONE_TIME;
    }

    public ExpenseRequest(Long userId, Long categoryId, BigDecimal amount, LocalDate date, String description, RecurringType recurringType) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.recurringType = recurringType != null ? recurringType : RecurringType.ONE_TIME;
    }

    public ExpenseRequest(Long userId, Long categoryId, BigDecimal amount, LocalDate date) {
        this(userId, categoryId, amount, date, null);
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RecurringType getRecurringType() {
        return recurringType;
    }

    public void setRecurringType(RecurringType recurringType) {
        this.recurringType = recurringType != null ? recurringType : RecurringType.ONE_TIME;
    }
}
