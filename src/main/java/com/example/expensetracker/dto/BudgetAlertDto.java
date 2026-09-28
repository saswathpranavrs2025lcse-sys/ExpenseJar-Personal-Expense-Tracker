package com.example.expensetracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BudgetAlertDto {

    private Long categoryId;
    private String categoryName;
    private boolean hasBudget;
    private BigDecimal budgetAmount;
    private BigDecimal totalSpent;
    private BigDecimal remainingBudget;
    private Double percentageUsed;
    private String alertLevel; // "NONE", "WARNING", "EXCEEDED", "NO_BUDGET"
    private String tier; // "NORMAL", "APPROACHING", "WARNING", "EXCEEDED"
    private boolean warning;
    private String message;

    public BudgetAlertDto() {
    }

    public static BudgetAlertDto noBudget(BigDecimal totalSpent) {
        BudgetAlertDto dto = new BudgetAlertDto();
        dto.setHasBudget(false);
        dto.setTotalSpent(totalSpent);
        dto.setAlertLevel("NO_BUDGET");
        dto.setTier("NORMAL");
        dto.setWarning(false);
        dto.setMessage("No budget set for this category and month.");
        return dto;
    }

    public static BudgetAlertDto calculated(
        String categoryName,
        BigDecimal budgetAmount,
        BigDecimal totalSpent,
        BigDecimal remainingBudget,
        double percentageUsed
    ) {
        BudgetAlertDto dto = new BudgetAlertDto();
        dto.setCategoryName(categoryName);
        dto.setHasBudget(true);
        dto.setBudgetAmount(budgetAmount);
        dto.setTotalSpent(totalSpent);
        dto.setRemainingBudget(remainingBudget);
        double roundedPct = Math.round(percentageUsed * 10.0) / 10.0;
        dto.setPercentageUsed(roundedPct);

        if (percentageUsed >= 100.0) {
            dto.setAlertLevel("EXCEEDED");
            dto.setTier("EXCEEDED");
            dto.setWarning(true);
            dto.setMessage(String.format("Budget exceeded. %s spending has reached %.1f%% of the monthly budget.", categoryName, roundedPct));
        } else if (percentageUsed >= 90.0) {
            dto.setAlertLevel("WARNING");
            dto.setTier("WARNING");
            dto.setWarning(true);
            dto.setMessage(String.format("Warning: %s spending has reached %.1f%% of the monthly budget.", categoryName, roundedPct));
        } else if (percentageUsed >= 75.0) {
            dto.setAlertLevel("NONE");
            dto.setTier("APPROACHING");
            dto.setWarning(false);
            dto.setMessage(String.format("%s spending is within budget (%.1f%% used). Approaching limit.", categoryName, roundedPct));
        } else {
            dto.setAlertLevel("NONE");
            dto.setTier("NORMAL");
            dto.setWarning(false);
            dto.setMessage(String.format("%s spending is within budget (%.1f%% used).", categoryName, roundedPct));
        }
        return dto;
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

    public boolean isHasBudget() {
        return hasBudget;
    }

    public void setHasBudget(boolean hasBudget) {
        this.hasBudget = hasBudget;
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

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public boolean isWarning() {
        return warning;
    }

    public void setWarning(boolean warning) {
        this.warning = warning;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
