package com.example.expensetracker.service;

import com.example.expensetracker.dto.BudgetAlertDto;
import com.example.expensetracker.dto.ExpenseRequest;
import com.example.expensetracker.dto.ExpenseResponse;
import com.example.expensetracker.entity.Budget;
import com.example.expensetracker.entity.Category;
import com.example.expensetracker.entity.Expense;
import com.example.expensetracker.entity.RecurringType;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.exception.InvalidExpenseException;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.repository.BudgetRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.security.SecurityUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final BudgetRepository budgetRepository;

    public ExpenseService(
        ExpenseRepository expenseRepository,
        UserService userService,
        CategoryService categoryService,
        BudgetRepository budgetRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.userService = userService;
        this.categoryService = categoryService;
        this.budgetRepository = budgetRepository;
    }

    public ExpenseResponse createExpense(ExpenseRequest request) {
        validateExpenseAmount(request.getAmount());
        if (request.getDate() == null) {
            throw new InvalidExpenseException("Expense date cannot be null");
        }

        // Enforce user existence first (throws ResourceNotFoundException 404 if user does not exist)
        Long effectiveUserId = request.getUserId();
        if (effectiveUserId == null) {
            effectiveUserId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new InvalidExpenseException("User ID is required"));
        }

        User user = userService.getUserEntityById(effectiveUserId);

        // Enforce that authenticated user cannot log expenses for another user
        Optional<Long> authUserId = SecurityUtils.getCurrentUserId();
        if (authUserId.isPresent() && !authUserId.get().equals(effectiveUserId)) {
            throw new AccessDeniedException("You do not have permission to log expenses for another user.");
        }

        Category category = categoryService.getCategoryEntityById(request.getCategoryId());

        Expense expense = new Expense();
        expense.setUser(user);
        expense.setCategory(category);
        expense.setAmount(request.getAmount());
        expense.setDate(request.getDate());
        expense.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        expense.setRecurringType(request.getRecurringType() != null ? request.getRecurringType() : RecurringType.ONE_TIME);

        Expense saved = expenseRepository.save(expense);

        BudgetAlertDto alert = evaluateBudgetAlert(user.getId(), category.getId(), category.getName(), saved.getDate());

        ExpenseResponse response = mapToResponse(saved);
        response.setBudgetAlert(alert);
        return response;
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id) {
        Expense expense = getExpenseEntityById(id);
        enforceExpenseOwnership(expense);

        BudgetAlertDto alert = evaluateBudgetAlert(
            expense.getUser().getId(),
            expense.getCategory().getId(),
            expense.getCategory().getName(),
            expense.getDate()
        );
        ExpenseResponse response = mapToResponse(expense);
        response.setBudgetAlert(alert);
        return response;
    }

    @Transactional(readOnly = true)
    public Expense getExpenseEntityById(Long id) {
        return expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAllExpenses(
        Long userId,
        Long categoryId,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        String search
    ) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new InvalidExpenseException("Start date cannot be after end date");
        }

        // Automatically scope to authenticated user if present
        Long effectiveUserId = SecurityUtils.getCurrentUserId().orElse(userId);

        String trimmedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        List<Expense> expenses;
        if (minAmount != null || maxAmount != null || trimmedSearch != null) {
            expenses = expenseRepository.findWithAdvancedFilters(effectiveUserId, categoryId, startDate, endDate, minAmount, maxAmount, trimmedSearch);
        } else {
            expenses = expenseRepository.findWithFilters(effectiveUserId, categoryId, startDate, endDate);
        }

        return expenses.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<ExpenseResponse> getAllExpenses(Long userId, Long categoryId, LocalDate startDate, LocalDate endDate) {
        return getAllExpenses(userId, categoryId, startDate, endDate, null, null, null);
    }

    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Expense existing = getExpenseEntityById(id);
        enforceExpenseOwnership(existing);

        validateExpenseAmount(request.getAmount());
        if (request.getDate() == null) {
            throw new InvalidExpenseException("Expense date cannot be null");
        }

        Long effectiveUserId = SecurityUtils.getCurrentUserId().orElse(request.getUserId() != null ? request.getUserId() : existing.getUser().getId());
        User user = userService.getUserEntityById(effectiveUserId);
        Category category = categoryService.getCategoryEntityById(request.getCategoryId());

        existing.setUser(user);
        existing.setCategory(category);
        existing.setAmount(request.getAmount());
        existing.setDate(request.getDate());
        existing.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        if (request.getRecurringType() != null) {
            existing.setRecurringType(request.getRecurringType());
        }

        Expense updated = expenseRepository.save(existing);
        BudgetAlertDto alert = evaluateBudgetAlert(user.getId(), category.getId(), category.getName(), updated.getDate());

        ExpenseResponse response = mapToResponse(updated);
        response.setBudgetAlert(alert);
        return response;
    }

    public void deleteExpense(Long id) {
        Expense expense = getExpenseEntityById(id);
        enforceExpenseOwnership(expense);
        expenseRepository.delete(expense);
    }

    @Transactional(readOnly = true)
    public byte[] exportExpensesToCsv(Long userId, Long categoryId, LocalDate startDate, LocalDate endDate, BigDecimal minAmount, BigDecimal maxAmount, String search) {
        List<ExpenseResponse> list = getAllExpenses(userId, categoryId, startDate, endDate, minAmount, maxAmount, search);

        StringBuilder sb = new StringBuilder();
        sb.append("Date,Category,Amount (INR),Description,Recurring\n");

        for (ExpenseResponse e : list) {
            sb.append(e.getDate() != null ? e.getDate().toString() : "").append(",");
            sb.append(escapeCsv(e.getCategoryName())).append(",");
            sb.append(e.getAmount() != null ? e.getAmount().toPlainString() : "0.00").append(",");
            sb.append(escapeCsv(e.getDescription() != null ? e.getDescription() : "")).append(",");
            sb.append(e.getRecurringType() != null ? e.getRecurringType().name() : "ONE_TIME").append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        if (value == null) return "\"\"";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return "\"" + value + "\"";
    }

    private void enforceExpenseOwnership(Expense expense) {
        Optional<Long> currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId.isPresent() && !expense.getUser().getId().equals(currentUserId.get())) {
            throw new AccessDeniedException("You do not have permission to access another user's expense record.");
        }
    }

    public BudgetAlertDto evaluateBudgetAlert(Long userId, Long categoryId, String categoryName, LocalDate expenseDate) {
        YearMonth ym = YearMonth.from(expenseDate);
        String monthStr = ym.toString();
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        BigDecimal totalSpent = expenseRepository.calculateTotalSpending(userId, categoryId, startDate, endDate);
        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }

        Optional<Budget> budgetOpt = budgetRepository.findByUserIdAndCategoryIdAndMonth(userId, categoryId, monthStr);
        if (budgetOpt.isEmpty() || budgetOpt.get().getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return BudgetAlertDto.noBudget(totalSpent);
        }

        BigDecimal budgetAmount = budgetOpt.get().getAmount();
        BigDecimal remaining = budgetAmount.subtract(totalSpent);
        double percentage = totalSpent.multiply(BigDecimal.valueOf(100))
            .divide(budgetAmount, 2, RoundingMode.HALF_UP)
            .doubleValue();

        return BudgetAlertDto.calculated(categoryName, budgetAmount, totalSpent, remaining, percentage);
    }

    private void validateExpenseAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidExpenseException("Expense amount must be greater than zero");
        }
    }

    public ExpenseResponse mapToResponse(Expense expense) {
        return new ExpenseResponse(
            expense.getId(),
            expense.getAmount(),
            expense.getDate(),
            expense.getDescription(),
            expense.getUser().getId(),
            expense.getUser().getName(),
            expense.getCategory().getId(),
            expense.getCategory().getName(),
            expense.getRecurringType()
        );
    }
}
