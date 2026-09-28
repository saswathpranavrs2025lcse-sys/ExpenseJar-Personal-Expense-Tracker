package com.example.expensetracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private BudgetService budgetService;

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "John", "john@example.com");
        testCategory = new Category(1L, "Food", "Food expenses");
    }

    @Test
    @DisplayName("Create budget successfully")
    void testCreateBudgetSuccessfully() {
        BudgetRequest request = new BudgetRequest(1L, 1L, new BigDecimal("5000.00"), "2026-09");
        Budget savedBudget = new Budget(10L, new BigDecimal("5000.00"), "2026-09", testUser, testCategory);

        when(userService.getUserEntityById(1L)).thenReturn(testUser);
        when(categoryService.getCategoryEntityById(1L)).thenReturn(testCategory);
        when(budgetRepository.existsByUserIdAndCategoryIdAndMonth(1L, 1L, "2026-09")).thenReturn(false);
        when(budgetRepository.save(any(Budget.class))).thenReturn(savedBudget);

        BudgetResponse response = budgetService.createBudget(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(new BigDecimal("5000.00"), response.getAmount());
        assertEquals("2026-09", response.getMonth());
    }

    @Test
    @DisplayName("Budget amount = 0 throws InvalidBudgetException")
    void testBudgetAmountZeroThrowsException() {
        BudgetRequest request = new BudgetRequest(1L, 1L, BigDecimal.ZERO, "2026-09");
        assertThrows(InvalidBudgetException.class, () -> budgetService.createBudget(request));
    }

    @Test
    @DisplayName("Budget amount < 0 throws InvalidBudgetException")
    void testBudgetAmountNegativeThrowsException() {
        BudgetRequest request = new BudgetRequest(1L, 1L, new BigDecimal("-500.00"), "2026-09");
        assertThrows(InvalidBudgetException.class, () -> budgetService.createBudget(request));
    }

    @Test
    @DisplayName("Duplicate budget throws DuplicateResourceException")
    void testDuplicateBudgetThrowsException() {
        BudgetRequest request = new BudgetRequest(1L, 1L, new BigDecimal("5000.00"), "2026-09");
        when(userService.getUserEntityById(1L)).thenReturn(testUser);
        when(categoryService.getCategoryEntityById(1L)).thenReturn(testCategory);
        when(budgetRepository.existsByUserIdAndCategoryIdAndMonth(1L, 1L, "2026-09")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () -> budgetService.createBudget(request));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("Non-existing budget by ID throws ResourceNotFoundException")
    void testNonExistingBudgetThrowsException() {
        when(budgetRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> budgetService.getBudgetById(999L));
    }

    @Test
    @DisplayName("Calculate budget status")
    void testGetBudgetStatus() {
        when(userService.getUserEntityById(1L)).thenReturn(testUser);
        when(categoryService.getCategoryEntityById(1L)).thenReturn(testCategory);
        when(budgetRepository.findByUserIdAndCategoryIdAndMonth(1L, 1L, "2026-09"))
            .thenReturn(Optional.of(new Budget(1L, new BigDecimal("5000.00"), "2026-09", testUser, testCategory)));
        when(expenseRepository.calculateTotalSpending(1L, 1L, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))
            .thenReturn(new BigDecimal("4200.00"));

        BudgetStatusResponse status = budgetService.getBudgetStatus(1L, 1L, "2026-09");

        assertNotNull(status);
        assertEquals(new BigDecimal("5000.00"), status.getBudgetAmount());
        assertEquals(new BigDecimal("4200.00"), status.getTotalSpent());
        assertEquals(new BigDecimal("800.00"), status.getRemainingBudget());
        assertEquals(84.0, status.getPercentageUsed());
        assertEquals("NONE", status.getAlertLevel());
    }

    @Test
    @DisplayName("Delete budget")
    void testDeleteBudget() {
        Budget budget = new Budget(3L, new BigDecimal("2000.00"), "2026-09", testUser, testCategory);
        when(budgetRepository.findById(3L)).thenReturn(Optional.of(budget));

        budgetService.deleteBudget(3L);

        verify(budgetRepository).delete(budget);
    }
}
