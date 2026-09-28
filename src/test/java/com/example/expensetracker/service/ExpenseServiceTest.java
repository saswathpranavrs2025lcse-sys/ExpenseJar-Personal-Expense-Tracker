package com.example.expensetracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.expensetracker.dto.BudgetAlertDto;
import com.example.expensetracker.dto.ExpenseRequest;
import com.example.expensetracker.dto.ExpenseResponse;
import com.example.expensetracker.entity.Budget;
import com.example.expensetracker.entity.Category;
import com.example.expensetracker.entity.Expense;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.exception.InvalidExpenseException;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.repository.BudgetRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private User testUser;
    private Category testCategory;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "John", "john@example.com");
        testCategory = new Category(1L, "Food", "Food expenses");
        testDate = LocalDate.of(2026, 9, 15);
    }

    @Test
    @DisplayName("Create expense successfully")
    void testCreateExpenseSuccessfully() {
        ExpenseRequest request = new ExpenseRequest(1L, 1L, new BigDecimal("500.00"), testDate, "Lunch");
        Expense savedExpense = new Expense(10L, new BigDecimal("500.00"), testDate, "Lunch", testUser, testCategory);

        when(userService.getUserEntityById(1L)).thenReturn(testUser);
        when(categoryService.getCategoryEntityById(1L)).thenReturn(testCategory);
        when(expenseRepository.save(any(Expense.class))).thenReturn(savedExpense);
        when(expenseRepository.calculateTotalSpending(eq(1L), eq(1L), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(new BigDecimal("500.00"));
        when(budgetRepository.findByUserIdAndCategoryIdAndMonth(eq(1L), eq(1L), eq("2026-09")))
            .thenReturn(Optional.of(new Budget(1L, new BigDecimal("5000.00"), "2026-09", testUser, testCategory)));

        ExpenseResponse response = expenseService.createExpense(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(new BigDecimal("500.00"), response.getAmount());
        assertEquals("John", response.getUserName());
        assertEquals("Food", response.getCategoryName());
        assertNotNull(response.getBudgetAlert());
        assertEquals("NONE", response.getBudgetAlert().getAlertLevel());
        assertFalse(response.getBudgetAlert().isWarning());
    }

    @Test
    @DisplayName("Expense amount = 0 throws InvalidExpenseException")
    void testExpenseAmountZeroThrowsException() {
        ExpenseRequest request = new ExpenseRequest(1L, 1L, BigDecimal.ZERO, testDate);
        InvalidExpenseException ex = assertThrows(InvalidExpenseException.class, () -> expenseService.createExpense(request));
        assertTrue(ex.getMessage().contains("greater than zero"));
    }

    @Test
    @DisplayName("Expense amount < 0 throws InvalidExpenseException")
    void testExpenseAmountNegativeThrowsException() {
        ExpenseRequest request = new ExpenseRequest(1L, 1L, new BigDecimal("-50.00"), testDate);
        InvalidExpenseException ex = assertThrows(InvalidExpenseException.class, () -> expenseService.createExpense(request));
        assertTrue(ex.getMessage().contains("greater than zero"));
    }

    @Test
    @DisplayName("Non-existing user throws ResourceNotFoundException")
    void testNonExistingUserThrowsException() {
        ExpenseRequest request = new ExpenseRequest(999L, 1L, new BigDecimal("200.00"), testDate);
        when(userService.getUserEntityById(999L)).thenThrow(new ResourceNotFoundException("User not found with id: 999"));

        assertThrows(ResourceNotFoundException.class, () -> expenseService.createExpense(request));
    }

    @Test
    @DisplayName("Non-existing category throws ResourceNotFoundException")
    void testNonExistingCategoryThrowsException() {
        ExpenseRequest request = new ExpenseRequest(1L, 999L, new BigDecimal("200.00"), testDate);
        when(userService.getUserEntityById(1L)).thenReturn(testUser);
        when(categoryService.getCategoryEntityById(999L)).thenThrow(new ResourceNotFoundException("Category not found with id: 999"));

        assertThrows(ResourceNotFoundException.class, () -> expenseService.createExpense(request));
    }

    @Test
    @DisplayName("Spending below 90% triggers no warning")
    void testSpendingBelow90Percent() {
        Budget budget = new Budget(1L, new BigDecimal("1000.00"), "2026-09", testUser, testCategory);
        when(budgetRepository.findByUserIdAndCategoryIdAndMonth(1L, 1L, "2026-09")).thenReturn(Optional.of(budget));
        when(expenseRepository.calculateTotalSpending(eq(1L), eq(1L), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(new BigDecimal("850.00")); // 85%

        BudgetAlertDto alert = expenseService.evaluateBudgetAlert(1L, 1L, "Food", testDate);

        assertNotNull(alert);
        assertTrue(alert.isHasBudget());
        assertEquals("NONE", alert.getAlertLevel());
        assertFalse(alert.isWarning());
        assertEquals(85.0, alert.getPercentageUsed());
    }

    @Test
    @DisplayName("Spending exactly at 90% triggers WARNING")
    void testSpendingExactlyAt90Percent() {
        Budget budget = new Budget(1L, new BigDecimal("1000.00"), "2026-09", testUser, testCategory);
        when(budgetRepository.findByUserIdAndCategoryIdAndMonth(1L, 1L, "2026-09")).thenReturn(Optional.of(budget));
        when(expenseRepository.calculateTotalSpending(eq(1L), eq(1L), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(new BigDecimal("900.00")); // 90%

        BudgetAlertDto alert = expenseService.evaluateBudgetAlert(1L, 1L, "Food", testDate);

        assertNotNull(alert);
        assertEquals("WARNING", alert.getAlertLevel());
        assertTrue(alert.isWarning());
        assertEquals(90.0, alert.getPercentageUsed());
        assertTrue(alert.getMessage().contains("Warning: Food spending has reached 90.0%"));
    }

    @Test
    @DisplayName("Spending above 90% and below 100% triggers WARNING")
    void testSpendingAbove90Percent() {
        Budget budget = new Budget(1L, new BigDecimal("5000.00"), "2026-09", testUser, testCategory);
        when(budgetRepository.findByUserIdAndCategoryIdAndMonth(1L, 1L, "2026-09")).thenReturn(Optional.of(budget));
        when(expenseRepository.calculateTotalSpending(eq(1L), eq(1L), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(new BigDecimal("4600.00")); // 92%

        BudgetAlertDto alert = expenseService.evaluateBudgetAlert(1L, 1L, "Food", testDate);

        assertNotNull(alert);
        assertEquals("WARNING", alert.getAlertLevel());
        assertTrue(alert.isWarning());
        assertEquals(92.0, alert.getPercentageUsed());
        assertTrue(alert.getMessage().contains("92.0% of the monthly budget"));
    }

    @Test
    @DisplayName("Spending exactly at 100% triggers EXCEEDED")
    void testSpendingExactlyAt100Percent() {
        Budget budget = new Budget(1L, new BigDecimal("1000.00"), "2026-09", testUser, testCategory);
        when(budgetRepository.findByUserIdAndCategoryIdAndMonth(1L, 1L, "2026-09")).thenReturn(Optional.of(budget));
        when(expenseRepository.calculateTotalSpending(eq(1L), eq(1L), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(new BigDecimal("1000.00")); // 100%

        BudgetAlertDto alert = expenseService.evaluateBudgetAlert(1L, 1L, "Food", testDate);

        assertNotNull(alert);
        assertEquals("EXCEEDED", alert.getAlertLevel());
        assertTrue(alert.isWarning());
        assertEquals(100.0, alert.getPercentageUsed());
        assertTrue(alert.getMessage().contains("Budget exceeded"));
    }

    @Test
    @DisplayName("Spending above 100% triggers EXCEEDED")
    void testSpendingAbove100Percent() {
        Budget budget = new Budget(1L, new BigDecimal("1000.00"), "2026-09", testUser, testCategory);
        when(budgetRepository.findByUserIdAndCategoryIdAndMonth(1L, 1L, "2026-09")).thenReturn(Optional.of(budget));
        when(expenseRepository.calculateTotalSpending(eq(1L), eq(1L), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(new BigDecimal("1250.00")); // 125%

        BudgetAlertDto alert = expenseService.evaluateBudgetAlert(1L, 1L, "Food", testDate);

        assertNotNull(alert);
        assertEquals("EXCEEDED", alert.getAlertLevel());
        assertTrue(alert.isWarning());
        assertEquals(125.0, alert.getPercentageUsed());
        assertTrue(alert.getMessage().contains("Budget exceeded"));
    }

    @Test
    @DisplayName("Delete expense successfully")
    void testDeleteExpense() {
        Expense expense = new Expense(5L, new BigDecimal("100.00"), testDate, "Test", testUser, testCategory);
        when(expenseRepository.findById(5L)).thenReturn(Optional.of(expense));

        expenseService.deleteExpense(5L);

        verify(expenseRepository).delete(expense);
    }
}
