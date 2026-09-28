package com.example.expensetracker.service;

import com.example.expensetracker.dto.BudgetRequest;
import com.example.expensetracker.dto.BudgetResponse;
import com.example.expensetracker.dto.BudgetStatusResponse;
import com.example.expensetracker.entity.Budget;
import com.example.expensetracker.entity.Category;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.exception.DuplicateResourceException;
import com.example.expensetracker.exception.InvalidBudgetException;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.repository.BudgetRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.security.SecurityUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final ExpenseRepository expenseRepository;

    public BudgetService(
        BudgetRepository budgetRepository,
        UserService userService,
        CategoryService categoryService,
        ExpenseRepository expenseRepository
    ) {
        this.budgetRepository = budgetRepository;
        this.userService = userService;
        this.categoryService = categoryService;
        this.expenseRepository = expenseRepository;
    }

    public BudgetResponse createBudget(BudgetRequest request) {
        validateBudgetAmount(request.getAmount());
        validateMonthFormat(request.getMonth());

        Long effectiveUserId = request.getUserId();
        if (effectiveUserId == null) {
            effectiveUserId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new InvalidBudgetException("User ID is required"));
        }

        User user = userService.getUserEntityById(effectiveUserId);

        Optional<Long> authUserId = SecurityUtils.getCurrentUserId();
        if (authUserId.isPresent() && !authUserId.get().equals(effectiveUserId)) {
            throw new AccessDeniedException("You do not have permission to manage budgets for another user.");
        }

        Category category = categoryService.getCategoryEntityById(request.getCategoryId());

        String month = request.getMonth().trim();
        if (budgetRepository.existsByUserIdAndCategoryIdAndMonth(user.getId(), category.getId(), month)) {
            throw new DuplicateResourceException(String.format(
                "A budget already exists for user '%s' in category '%s' for month %s",
                user.getName(), category.getName(), month
            ));
        }

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(category);
        budget.setAmount(request.getAmount());
        budget.setMonth(month);

        Budget saved = budgetRepository.save(budget);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(Long id) {
        Budget budget = getBudgetEntityById(id);
        enforceBudgetOwnership(budget);
        return mapToResponse(budget);
    }

    @Transactional(readOnly = true)
    public Budget getBudgetEntityById(Long id) {
        return budgetRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getAllBudgets(Long userId, Long categoryId, String month) {
        if (month != null && !month.trim().isEmpty()) {
            validateMonthFormat(month.trim());
        }

        Long effectiveUserId = SecurityUtils.getCurrentUserId().orElse(userId);

        return budgetRepository.findWithFilters(effectiveUserId, categoryId, (month != null && !month.trim().isEmpty()) ? month.trim() : null)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public BudgetResponse updateBudget(Long id, BudgetRequest request) {
        Budget existing = getBudgetEntityById(id);
        enforceBudgetOwnership(existing);

        validateBudgetAmount(request.getAmount());
        validateMonthFormat(request.getMonth());

        Long effectiveUserId = SecurityUtils.getCurrentUserId().orElse(request.getUserId() != null ? request.getUserId() : existing.getUser().getId());
        User user = userService.getUserEntityById(effectiveUserId);
        Category category = categoryService.getCategoryEntityById(request.getCategoryId());
        String month = request.getMonth().trim();

        if (budgetRepository.existsByUserIdAndCategoryIdAndMonthAndIdNot(user.getId(), category.getId(), month, id)) {
            throw new DuplicateResourceException(String.format(
                "Another budget already exists for user '%s' in category '%s' for month %s",
                user.getName(), category.getName(), month
            ));
        }

        existing.setUser(user);
        existing.setCategory(category);
        existing.setAmount(request.getAmount());
        existing.setMonth(month);

        Budget updated = budgetRepository.save(existing);
        return mapToResponse(updated);
    }

    public void deleteBudget(Long id) {
        Budget budget = getBudgetEntityById(id);
        enforceBudgetOwnership(budget);
        budgetRepository.delete(budget);
    }

    @Transactional(readOnly = true)
    public BudgetStatusResponse getBudgetStatus(Long userId, Long categoryId, String month) {
        validateMonthFormat(month);

        Long effectiveUserId = SecurityUtils.getCurrentUserId().orElse(userId);
        User user = userService.getUserEntityById(effectiveUserId);
        Category category = categoryService.getCategoryEntityById(categoryId);

        YearMonth ym = YearMonth.parse(month.trim());
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        BigDecimal totalSpent = expenseRepository.calculateTotalSpending(user.getId(), category.getId(), startDate, endDate);
        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }

        Optional<Budget> budgetOpt = budgetRepository.findByUserIdAndCategoryIdAndMonth(user.getId(), category.getId(), month.trim());
        BigDecimal budgetAmount = budgetOpt.map(Budget::getAmount).orElse(BigDecimal.ZERO);
        BigDecimal remaining = budgetAmount.subtract(totalSpent);

        double percentageUsed = 0.0;
        if (budgetAmount.compareTo(BigDecimal.ZERO) > 0) {
            percentageUsed = totalSpent.multiply(BigDecimal.valueOf(100))
                .divide(budgetAmount, 2, RoundingMode.HALF_UP)
                .doubleValue();
        }

        boolean warning = percentageUsed >= 90.0 && budgetAmount.compareTo(BigDecimal.ZERO) > 0;
        boolean exceeded = percentageUsed >= 100.0 && budgetAmount.compareTo(BigDecimal.ZERO) > 0;

        String alertLevel = "NONE";
        String alertMessage;
        if (budgetAmount.compareTo(BigDecimal.ZERO) == 0) {
            alertLevel = "NO_BUDGET";
            alertMessage = "No budget configured for this month";
        } else if (exceeded) {
            alertLevel = "EXCEEDED";
            alertMessage = String.format("Budget exceeded. %s spending has reached %.1f%% of the monthly budget.", category.getName(), percentageUsed);
        } else if (warning) {
            alertLevel = "WARNING";
            alertMessage = String.format("Warning: %s spending has reached %.1f%% of the monthly budget.", category.getName(), percentageUsed);
        } else {
            alertLevel = "NONE";
            alertMessage = String.format("%s spending is within budget (%.1f%% used).", category.getName(), percentageUsed);
        }

        BudgetStatusResponse response = new BudgetStatusResponse();
        response.setUserId(user.getId());
        response.setUserName(user.getName());
        response.setCategoryId(category.getId());
        response.setCategoryName(category.getName());
        response.setMonth(month.trim());
        response.setBudgetAmount(budgetAmount);
        response.setTotalSpent(totalSpent);
        response.setRemainingBudget(remaining);
        response.setPercentageUsed(Math.round(percentageUsed * 10.0) / 10.0);
        response.setAlertLevel(alertLevel);
        response.setWarning(warning);
        response.setAlertMessage(alertMessage);

        return response;
    }

    private void enforceBudgetOwnership(Budget budget) {
        Optional<Long> currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId.isPresent() && !budget.getUser().getId().equals(currentUserId.get())) {
            throw new AccessDeniedException("You do not have permission to access another user's budget record.");
        }
    }

    private void validateBudgetAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBudgetException("Budget amount must be greater than zero");
        }
    }

    private void validateMonthFormat(String month) {
        if (month == null || month.trim().isEmpty()) {
            throw new InvalidBudgetException("Budget month cannot be blank");
        }
        try {
            YearMonth.parse(month.trim());
        } catch (DateTimeParseException ex) {
            throw new InvalidBudgetException("Budget month must be in valid YYYY-MM format (e.g. 2026-09)");
        }
    }

    private BudgetResponse mapToResponse(Budget budget) {
        return new BudgetResponse(
            budget.getId(),
            budget.getUser().getId(),
            budget.getUser().getName(),
            budget.getCategory().getId(),
            budget.getCategory().getName(),
            budget.getAmount(),
            budget.getMonth()
        );
    }
}
