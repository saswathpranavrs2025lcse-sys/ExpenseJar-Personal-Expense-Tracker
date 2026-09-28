package com.example.expensetracker.service;

import com.example.expensetracker.dto.BudgetAlertDto;
import com.example.expensetracker.dto.DashboardResponse;
import com.example.expensetracker.dto.ExpenseResponse;
import com.example.expensetracker.dto.ExpenseSummaryResponse;
import com.example.expensetracker.dto.MonthlySpendingItem;
import com.example.expensetracker.dto.PersonalDashboardResponse;
import com.example.expensetracker.dto.SpendingTrendResponse;
import com.example.expensetracker.entity.Budget;
import com.example.expensetracker.entity.Category;
import com.example.expensetracker.entity.Expense;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.exception.InvalidBudgetException;
import com.example.expensetracker.repository.BudgetRepository;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.repository.projection.CategorySpendingProjection;
import com.example.expensetracker.repository.projection.MonthlySpendingProjection;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ExpenseReportService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    public ExpenseReportService(
        ExpenseRepository expenseRepository,
        BudgetRepository budgetRepository,
        UserRepository userRepository,
        CategoryRepository categoryRepository,
        UserService userService,
        CategoryService categoryService
    ) {
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    public List<ExpenseSummaryResponse> getCurrentMonthCategorySummaries(Long userId) {
        YearMonth currentMonth = YearMonth.now();
        return getMonthCategorySummaries(userId, currentMonth);
    }

    public List<ExpenseSummaryResponse> getMonthCategorySummaries(Long userId, YearMonth yearMonth) {
        User user = userService.getUserEntityById(userId);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        String monthStr = yearMonth.toString();

        List<CategorySpendingProjection> spendingList = expenseRepository.findCategorySpendingBreakdown(user.getId(), startDate, endDate);
        Map<Long, BigDecimal> spentMap = new HashMap<>();
        Map<Long, String> nameMap = new HashMap<>();
        for (CategorySpendingProjection p : spendingList) {
            spentMap.put(p.getCategoryId(), p.getTotalSpent());
            nameMap.put(p.getCategoryId(), p.getCategoryName());
        }

        List<Budget> budgets = budgetRepository.findByUserIdAndMonth(user.getId(), monthStr);
        Map<Long, BigDecimal> budgetMap = new HashMap<>();
        for (Budget b : budgets) {
            budgetMap.put(b.getCategory().getId(), b.getAmount());
            nameMap.put(b.getCategory().getId(), b.getCategory().getName());
        }

        List<Category> allCategories = categoryRepository.findAll();
        Set<Long> categoryIdsToInclude = new HashSet<>();
        for (Category c : allCategories) {
            categoryIdsToInclude.add(c.getId());
            nameMap.put(c.getId(), c.getName());
        }
        categoryIdsToInclude.addAll(spentMap.keySet());
        categoryIdsToInclude.addAll(budgetMap.keySet());

        List<ExpenseSummaryResponse> responses = new ArrayList<>();
        for (Long catId : categoryIdsToInclude) {
            BigDecimal totalSpent = spentMap.getOrDefault(catId, BigDecimal.ZERO);
            BigDecimal budgetAmount = budgetMap.getOrDefault(catId, BigDecimal.ZERO);
            BigDecimal remaining = budgetAmount.subtract(totalSpent);

            double percentageUsed = 0.0;
            if (budgetAmount.compareTo(BigDecimal.ZERO) > 0) {
                percentageUsed = totalSpent.multiply(BigDecimal.valueOf(100))
                    .divide(budgetAmount, 2, RoundingMode.HALF_UP)
                    .doubleValue();
            }

            ExpenseSummaryResponse summary = new ExpenseSummaryResponse();
            summary.setUserId(user.getId());
            summary.setUserName(user.getName());
            summary.setCategoryId(catId);
            summary.setCategoryName(nameMap.getOrDefault(catId, "Unknown"));
            summary.setMonth(monthStr);
            summary.setTotalSpent(totalSpent);
            summary.setBudgetAmount(budgetAmount);
            summary.setRemainingAmount(remaining);
            summary.setPercentageUsed(Math.round(percentageUsed * 10.0) / 10.0);
            summary.setWarning(percentageUsed >= 90.0 && budgetAmount.compareTo(BigDecimal.ZERO) > 0);
            summary.setExceeded(percentageUsed >= 100.0 && budgetAmount.compareTo(BigDecimal.ZERO) > 0);

            if (budgetAmount.compareTo(BigDecimal.ZERO) == 0) {
                summary.setStatusMessage("No budget set");
            } else if (summary.isExceeded()) {
                summary.setStatusMessage(String.format("Budget exceeded. %s spending has exceeded its monthly limit (%.1f%% used).", summary.getCategoryName(), percentageUsed));
            } else if (summary.isWarning()) {
                summary.setStatusMessage(String.format("⚠ %s budget is at %.0f%%.", summary.getCategoryName(), percentageUsed));
            } else if (percentageUsed >= 75.0) {
                summary.setStatusMessage(String.format("%s budget is approaching limit (%.1f%% used).", summary.getCategoryName(), percentageUsed));
            } else {
                summary.setStatusMessage(String.format("%s spending is normal (%.1f%% used).", summary.getCategoryName(), percentageUsed));
            }

            responses.add(summary);
        }

        responses.sort((a, b) -> b.getTotalSpent().compareTo(a.getTotalSpent()));
        return responses;
    }

    public ExpenseSummaryResponse getCategorySummary(Long userId, Long categoryId, String month) {
        User user = userService.getUserEntityById(userId);
        Category category = categoryService.getCategoryEntityById(categoryId);

        YearMonth ym;
        try {
            ym = YearMonth.parse(month.trim());
        } catch (DateTimeParseException ex) {
            throw new InvalidBudgetException("Month must be in YYYY-MM format");
        }

        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        BigDecimal totalSpent = expenseRepository.calculateTotalSpending(user.getId(), category.getId(), startDate, endDate);
        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }

        Optional<Budget> budgetOpt = budgetRepository.findByUserIdAndCategoryIdAndMonth(user.getId(), category.getId(), ym.toString());
        BigDecimal budgetAmount = budgetOpt.map(Budget::getAmount).orElse(BigDecimal.ZERO);
        BigDecimal remaining = budgetAmount.subtract(totalSpent);

        double percentageUsed = 0.0;
        if (budgetAmount.compareTo(BigDecimal.ZERO) > 0) {
            percentageUsed = totalSpent.multiply(BigDecimal.valueOf(100))
                .divide(budgetAmount, 2, RoundingMode.HALF_UP)
                .doubleValue();
        }

        ExpenseSummaryResponse response = new ExpenseSummaryResponse();
        response.setUserId(user.getId());
        response.setUserName(user.getName());
        response.setCategoryId(category.getId());
        response.setCategoryName(category.getName());
        response.setMonth(ym.toString());
        response.setTotalSpent(totalSpent);
        response.setBudgetAmount(budgetAmount);
        response.setRemainingAmount(remaining);
        response.setPercentageUsed(Math.round(percentageUsed * 10.0) / 10.0);
        response.setWarning(percentageUsed >= 90.0 && budgetAmount.compareTo(BigDecimal.ZERO) > 0);
        response.setExceeded(percentageUsed >= 100.0 && budgetAmount.compareTo(BigDecimal.ZERO) > 0);

        return response;
    }

    public SpendingTrendResponse getSpendingTrend(Long userId, Long categoryId) {
        User user = userService.getUserEntityById(userId);
        List<MonthlySpendingProjection> rawTrend = expenseRepository.findMonthlySpendingTrend(user.getId(), categoryId);

        List<MonthlySpendingItem> items = new ArrayList<>();
        for (MonthlySpendingProjection p : rawTrend) {
            String monthFormatted = String.format("%04d-%02d", p.getExpenseYear(), p.getExpenseMonth());
            items.add(new MonthlySpendingItem(monthFormatted, p.getTotal()));
        }

        SpendingTrendResponse response = new SpendingTrendResponse();
        response.setUserId(user.getId());
        response.setUserName(user.getName());
        response.setCategoryId(categoryId);
        if (categoryId != null) {
            Category category = categoryService.getCategoryEntityById(categoryId);
            response.setCategoryName(category.getName());
        }
        response.setMonthlySpending(items);

        if (items.size() >= 2) {
            BigDecimal latest = items.get(items.size() - 1).getTotalSpent();
            BigDecimal previous = items.get(items.size() - 2).getTotalSpent();
            BigDecimal diff = latest.subtract(previous);

            double percentageChange = 0.0;
            if (previous.compareTo(BigDecimal.ZERO) > 0) {
                percentageChange = diff.multiply(BigDecimal.valueOf(100))
                    .divide(previous, 2, RoundingMode.HALF_UP)
                    .doubleValue();
            }

            response.setLatestMonth(items.get(items.size() - 1).getMonth());
            response.setPreviousMonth(items.get(items.size() - 2).getMonth());
            response.setMonthOverMonthChange(diff);
            response.setPercentageChange(percentageChange);
            response.setSpendingTrendDirection(diff.compareTo(BigDecimal.ZERO) > 0 ? "INCREASED" : (diff.compareTo(BigDecimal.ZERO) < 0 ? "DECREASED" : "STABLE"));
        } else if (items.size() == 1) {
            response.setLatestMonth(items.get(0).getMonth());
            response.setMonthOverMonthChange(BigDecimal.ZERO);
            response.setPercentageChange(0.0);
            response.setSpendingTrendDirection("FIRST_MONTH");
        } else {
            response.setSpendingTrendDirection("NO_DATA");
        }

        return response;
    }

    public PersonalDashboardResponse getPersonalDashboard(Long userId, String monthStr) {
        User user = userService.getUserEntityById(userId);
        YearMonth ym;
        if (monthStr != null && !monthStr.trim().isEmpty()) {
            ym = YearMonth.parse(monthStr.trim());
        } else {
            ym = YearMonth.now();
        }

        String monthFormatted = ym.toString();
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        // 1. Get Category Summaries
        List<ExpenseSummaryResponse> summaries = getMonthCategorySummaries(user.getId(), ym);

        // 2. Compute aggregate monthly totals from summaries
        BigDecimal totalSpent = BigDecimal.ZERO;
        BigDecimal monthlyBudget = BigDecimal.ZERO;
        ExpenseSummaryResponse largestCat = null;

        for (ExpenseSummaryResponse s : summaries) {
            totalSpent = totalSpent.add(s.getTotalSpent());
            monthlyBudget = monthlyBudget.add(s.getBudgetAmount());
            if (largestCat == null || s.getTotalSpent().compareTo(largestCat.getTotalSpent()) > 0) {
                if (s.getTotalSpent().compareTo(BigDecimal.ZERO) > 0) {
                    largestCat = s;
                }
            }
        }

        BigDecimal remainingBudget = monthlyBudget.subtract(totalSpent);
        double budgetUsedPercentage = 0.0;
        if (monthlyBudget.compareTo(BigDecimal.ZERO) > 0) {
            budgetUsedPercentage = totalSpent.multiply(BigDecimal.valueOf(100))
                .divide(monthlyBudget, 2, RoundingMode.HALF_UP)
                .doubleValue();
        }

        // 3. Dynamic Monthly Financial Summary text (Requirement 9)
        String monthName = ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        StringBuilder summaryText = new StringBuilder();
        summaryText.append(String.format("You spent ₹%s this month.", formatAmount(totalSpent)));
        if (largestCat != null && largestCat.getTotalSpent().compareTo(BigDecimal.ZERO) > 0) {
            summaryText.append(String.format(" Your largest spending category was %s (₹%s).", largestCat.getCategoryName(), formatAmount(largestCat.getTotalSpent())));
        }
        if (monthlyBudget.compareTo(BigDecimal.ZERO) > 0) {
            if (remainingBudget.compareTo(BigDecimal.ZERO) >= 0) {
                summaryText.append(String.format(" You have ₹%s remaining from your monthly budget.", formatAmount(remainingBudget)));
            } else {
                summaryText.append(String.format(" You have exceeded your monthly budget by ₹%s.", formatAmount(remainingBudget.abs())));
            }
        }

        // 4. Smart Spending Insights (Requirement 10)
        List<String> insights = new ArrayList<>();
        if (largestCat != null && largestCat.getTotalSpent().compareTo(BigDecimal.ZERO) > 0) {
            insights.add(String.format("You spent the most on %s this month.", largestCat.getCategoryName()));
        }

        // Month-over-month comparison
        YearMonth prevYm = ym.minusMonths(1);
        BigDecimal prevMonthSpending = expenseRepository.calculateTotalSpendingForUserInPeriod(
            user.getId(), prevYm.atDay(1), prevYm.atEndOfMonth()
        );
        if (prevMonthSpending != null && prevMonthSpending.compareTo(BigDecimal.ZERO) > 0 && totalSpent.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = totalSpent.subtract(prevMonthSpending);
            double pctDiff = diff.multiply(BigDecimal.valueOf(100))
                .divide(prevMonthSpending, 1, RoundingMode.HALF_UP)
                .doubleValue();
            if (pctDiff > 0) {
                insights.add(String.format("Your spending increased by %.1f%% compared with last month.", pctDiff));
            } else if (pctDiff < 0) {
                insights.add(String.format("Your spending decreased by %.1f%% compared with last month.", Math.abs(pctDiff)));
            } else {
                insights.add("Your spending is identical compared with last month.");
            }
        }

        // Check category budget usage
        for (ExpenseSummaryResponse s : summaries) {
            if (s.getBudgetAmount().compareTo(BigDecimal.ZERO) > 0 && s.getPercentageUsed() >= 75.0) {
                if (s.getPercentageUsed() >= 100.0) {
                    insights.add(String.format("%s budget has exceeded its monthly limit (%.0f%% used).", s.getCategoryName(), s.getPercentageUsed()));
                } else {
                    insights.add(String.format("You have used %.0f%% of your %s budget.", s.getPercentageUsed(), s.getCategoryName()));
                }
            }
        }

        // Remaining budget insight
        if (monthlyBudget.compareTo(BigDecimal.ZERO) > 0 && remainingBudget.compareTo(BigDecimal.ZERO) > 0) {
            insights.add(String.format("You have ₹%s remaining from your monthly budget.", formatAmount(remainingBudget)));
        }

        // 5. Recent Expenses for User
        List<Expense> recent = expenseRepository.findTop10ByUserIdOrderByDateDescIdDesc(user.getId());
        List<ExpenseResponse> recentExpenses = recent.stream().map(e -> new ExpenseResponse(
            e.getId(),
            e.getAmount(),
            e.getDate(),
            e.getDescription(),
            e.getUser().getId(),
            e.getUser().getName(),
            e.getCategory().getId(),
            e.getCategory().getName(),
            e.getRecurringType()
        )).collect(Collectors.toList());

        // 6. Budget Alerts (Tiered: Normal <75%, Approaching 75-89%, Warning 90-99%, Exceeded 100%+)
        List<BudgetAlertDto> alerts = new ArrayList<>();
        for (ExpenseSummaryResponse s : summaries) {
            if (s.getBudgetAmount().compareTo(BigDecimal.ZERO) > 0 && s.getPercentageUsed() >= 75.0) {
                BudgetAlertDto alert = BudgetAlertDto.calculated(
                    s.getCategoryName(),
                    s.getBudgetAmount(),
                    s.getTotalSpent(),
                    s.getRemainingAmount(),
                    s.getPercentageUsed()
                );
                alert.setCategoryId(s.getCategoryId());
                alerts.add(alert);
            }
        }

        PersonalDashboardResponse res = new PersonalDashboardResponse();
        res.setMonth(monthFormatted);
        res.setTotalSpent(totalSpent);
        res.setMonthlyBudget(monthlyBudget);
        res.setRemainingBudget(remainingBudget);
        res.setBudgetUsedPercentage(Math.round(budgetUsedPercentage * 10.0) / 10.0);
        res.setLargestCategoryName(largestCat != null ? largestCat.getCategoryName() : "None");
        res.setLargestCategoryAmount(largestCat != null ? largestCat.getTotalSpent() : BigDecimal.ZERO);
        res.setSummaryText(summaryText.toString());
        res.setInsights(insights);
        res.setCategorySummaries(summaries);
        res.setRecentExpenses(recentExpenses);
        res.setBudgetAlerts(alerts);

        return res;
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "0";
        return amount.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    public DashboardResponse getDashboard() {
        long totalUsers = userRepository.count();
        long totalCategories = categoryRepository.count();
        long totalExpenses = expenseRepository.count();
        long totalBudgets = budgetRepository.count();

        List<Expense> allExpenses = expenseRepository.findAll();
        BigDecimal totalSpentAllTime = allExpenses.stream()
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardResponse(totalUsers, totalCategories, totalExpenses, totalBudgets, totalSpentAllTime);
    }
}
