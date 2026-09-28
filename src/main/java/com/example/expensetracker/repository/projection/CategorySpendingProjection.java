package com.example.expensetracker.repository.projection;

import java.math.BigDecimal;

public interface CategorySpendingProjection {
    Long getCategoryId();
    String getCategoryName();
    BigDecimal getTotalSpent();
}
