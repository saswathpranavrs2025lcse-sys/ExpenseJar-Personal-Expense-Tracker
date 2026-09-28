package com.example.expensetracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
    name = "expenses",
    indexes = {
        @Index(name = "idx_expense_user_date", columnList = "user_id, expense_date"),
        @Index(name = "idx_expense_user_category_date", columnList = "user_id, category_id, expense_date")
    }
)
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Expense amount cannot be null")
    @Positive(message = "Expense amount must be greater than zero")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Expense date cannot be null")
    @Column(name = "expense_date", nullable = false)
    private LocalDate date;

    @Column(length = 255)
    private String description;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Category is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurring_type", length = 20, nullable = false)
    private RecurringType recurringType = RecurringType.ONE_TIME;

    public Expense() {
    }

    public Expense(Long id, BigDecimal amount, LocalDate date, String description, User user, Category category) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.user = user;
        this.category = category;
        this.recurringType = RecurringType.ONE_TIME;
    }

    public Expense(BigDecimal amount, LocalDate date, String description, User user, Category category) {
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.user = user;
        this.category = category;
        this.recurringType = RecurringType.ONE_TIME;
    }

    public Expense(BigDecimal amount, LocalDate date, String description, User user, Category category, RecurringType recurringType) {
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.user = user;
        this.category = category;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public RecurringType getRecurringType() {
        return recurringType;
    }

    public void setRecurringType(RecurringType recurringType) {
        this.recurringType = recurringType != null ? recurringType : RecurringType.ONE_TIME;
    }
}
