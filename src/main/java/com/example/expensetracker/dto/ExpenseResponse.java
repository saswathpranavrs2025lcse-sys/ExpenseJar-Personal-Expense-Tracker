package com.example.expensetracker.dto;

import com.example.expensetracker.entity.RecurringType;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpenseResponse {

    private Long id;
    private BigDecimal amount;
    private LocalDate date;
    private String description;
    private Long userId;
    private String userName;
    private Long categoryId;
    private String categoryName;
    private RecurringType recurringType = RecurringType.ONE_TIME;
    private BudgetAlertDto budgetAlert;
    private String warningMessage;

    public ExpenseResponse() {
    }

    public ExpenseResponse(Long id, BigDecimal amount, LocalDate date, String description, Long userId, String userName, Long categoryId, String categoryName) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.userId = userId;
        this.userName = userName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.recurringType = RecurringType.ONE_TIME;
    }

    public ExpenseResponse(Long id, BigDecimal amount, LocalDate date, String description, Long userId, String userName, Long categoryId, String categoryName, RecurringType recurringType) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.userId = userId;
        this.userName = userName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.recurringType = recurringType != null ? recurringType : RecurringType.ONE_TIME;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDate getExpenseDate() {
        return date;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.date = expenseDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public RecurringType getRecurringType() {
        return recurringType;
    }

    public void setRecurringType(RecurringType recurringType) {
        this.recurringType = recurringType != null ? recurringType : RecurringType.ONE_TIME;
    }

    public BudgetAlertDto getBudgetAlert() {
        return budgetAlert;
    }

    public void setBudgetAlert(BudgetAlertDto budgetAlert) {
        this.budgetAlert = budgetAlert;
        if (budgetAlert != null && budgetAlert.isWarning()) {
            this.warningMessage = budgetAlert.getMessage();
        }
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }
}
