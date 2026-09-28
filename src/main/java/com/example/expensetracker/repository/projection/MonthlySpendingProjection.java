package com.example.expensetracker.repository.projection;

import java.math.BigDecimal;

public interface MonthlySpendingProjection {
    Integer getExpenseYear();
    Integer getExpenseMonth();
    BigDecimal getTotal();
}
