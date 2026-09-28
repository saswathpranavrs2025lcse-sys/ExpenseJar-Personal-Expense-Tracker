package com.example.expensetracker;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.expensetracker.dto.BudgetRequest;
import com.example.expensetracker.dto.CategoryRequest;
import com.example.expensetracker.dto.ExpenseRequest;
import com.example.expensetracker.dto.UserRequest;
import com.example.expensetracker.repository.BudgetRepository;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "tester@campus.edu", roles = {"USER"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExpenseTrackerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    private Long userId;
    private Long categoryId;

    @BeforeEach
    void setupTestData() throws Exception {
        // Clear all repositories for deterministic test state
        expenseRepository.deleteAll();
        budgetRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create base user and category for testing
        UserRequest userRequest = new UserRequest("Student Tester", "tester@campus.edu");
        MvcResult userResult = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isCreated())
            .andReturn();
        JsonNode userJson = objectMapper.readTree(userResult.getResponse().getContentAsString());
        userId = userJson.get("id").asLong();

        CategoryRequest categoryRequest = new CategoryRequest("Food", "Daily canteen and cafeteria meals");
        MvcResult categoryResult = mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryRequest)))
            .andExpect(status().isCreated())
            .andReturn();
        JsonNode catJson = objectMapper.readTree(categoryResult.getResponse().getContentAsString());
        categoryId = catJson.get("id").asLong();
    }

    // 1. Create user successfully
    @Test
    @Order(1)
    @DisplayName("1. Create user successfully")
    void testCreateUserSuccessfully() throws Exception {
        UserRequest request = new UserRequest("Mithil", "mithil@test.com");
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.name", is("Mithil")))
            .andExpect(jsonPath("$.email", is("mithil@test.com")));
    }

    // 2. Create category successfully
    @Test
    @Order(2)
    @DisplayName("2. Create category successfully")
    void testCreateCategorySuccessfully() throws Exception {
        CategoryRequest request = new CategoryRequest("Stationery", "Books and notebooks");
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.name", is("Stationery")));
    }

    // 3. Create expense successfully
    @Test
    @Order(3)
    @DisplayName("3. Create expense successfully")
    void testCreateExpenseSuccessfully() throws Exception {
        ExpenseRequest request = new ExpenseRequest(userId, categoryId, new BigDecimal("250.00"), LocalDate.now(), "Breakfast");
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.amount", is(250.00)))
            .andExpect(jsonPath("$.userId", is(userId.intValue())))
            .andExpect(jsonPath("$.categoryId", is(categoryId.intValue())));
    }

    // 4. Create budget successfully
    @Test
    @Order(4)
    @DisplayName("4. Create budget successfully")
    void testCreateBudgetSuccessfully() throws Exception {
        BudgetRequest request = new BudgetRequest(userId, categoryId, new BigDecimal("5000.00"), "2026-09");
        mockMvc.perform(post("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.amount", is(5000.00)))
            .andExpect(jsonPath("$.month", is("2026-09")));
    }

    // 5. Get users
    @Test
    @Order(5)
    @DisplayName("5. Get users")
    void testGetUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // 6. Get categories
    @Test
    @Order(6)
    @DisplayName("6. Get categories")
    void testGetCategories() throws Exception {
        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // 7. Get expenses
    @Test
    @Order(7)
    @DisplayName("7. Get expenses")
    void testGetExpenses() throws Exception {
        ExpenseRequest request = new ExpenseRequest(userId, categoryId, new BigDecimal("100.00"), LocalDate.now());
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/expenses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // 8. Get budgets
    @Test
    @Order(8)
    @DisplayName("8. Get budgets")
    void testGetBudgets() throws Exception {
        BudgetRequest request = new BudgetRequest(userId, categoryId, new BigDecimal("3000.00"), "2026-09");
        mockMvc.perform(post("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/budgets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // 9. Update expense
    @Test
    @Order(9)
    @DisplayName("9. Update expense")
    void testUpdateExpense() throws Exception {
        ExpenseRequest createReq = new ExpenseRequest(userId, categoryId, new BigDecimal("150.00"), LocalDate.now(), "Original");
        MvcResult result = mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();
        Long expId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        ExpenseRequest updateReq = new ExpenseRequest(userId, categoryId, new BigDecimal("200.00"), LocalDate.now(), "Updated note");
        mockMvc.perform(put("/api/expenses/" + expId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.amount", is(200.00)))
            .andExpect(jsonPath("$.description", is("Updated note")));
    }

    // 10. Delete expense
    @Test
    @Order(10)
    @DisplayName("10. Delete expense")
    void testDeleteExpense() throws Exception {
        ExpenseRequest createReq = new ExpenseRequest(userId, categoryId, new BigDecimal("150.00"), LocalDate.now());
        MvcResult result = mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();
        Long expId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/expenses/" + expId))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/expenses/" + expId))
            .andExpect(status().isNotFound());
    }

    // 11. Update budget
    @Test
    @Order(11)
    @DisplayName("11. Update budget")
    void testUpdateBudget() throws Exception {
        BudgetRequest createReq = new BudgetRequest(userId, categoryId, new BigDecimal("3000.00"), "2026-09");
        MvcResult result = mockMvc.perform(post("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();
        Long bId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        BudgetRequest updateReq = new BudgetRequest(userId, categoryId, new BigDecimal("4500.00"), "2026-09");
        mockMvc.perform(put("/api/budgets/" + bId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.amount", is(4500.00)));
    }

    // 12. Delete budget
    @Test
    @Order(12)
    @DisplayName("12. Delete budget")
    void testDeleteBudget() throws Exception {
        BudgetRequest createReq = new BudgetRequest(userId, categoryId, new BigDecimal("3000.00"), "2026-09");
        MvcResult result = mockMvc.perform(post("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();
        Long bId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/budgets/" + bId))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/budgets/" + bId))
            .andExpect(status().isNotFound());
    }

    // 13. Calculate current-month category spending
    @Test
    @Order(13)
    @DisplayName("13. Calculate current-month category spending")
    void testCalculateCurrentMonthCategorySpending() throws Exception {
        YearMonth currentYm = YearMonth.now();
        // Setup budget
        BudgetRequest budgetReq = new BudgetRequest(userId, categoryId, new BigDecimal("5000.00"), currentYm.toString());
        mockMvc.perform(post("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(budgetReq)))
            .andExpect(status().isCreated());

        // Setup expense
        ExpenseRequest expReq = new ExpenseRequest(userId, categoryId, new BigDecimal("2000.00"), currentYm.atDay(5));
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expReq)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/expenses/summary/current?userId=" + userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].category", is("Food")))
            .andExpect(jsonPath("$[0].totalSpent", is(2000.00)))
            .andExpect(jsonPath("$[0].budget", is(5000.00)))
            .andExpect(jsonPath("$[0].remaining", is(3000.00)))
            .andExpect(jsonPath("$[0].percentageUsed", is(40.0)));
    }

    // 14. Calculate monthly trend
    @Test
    @Order(14)
    @DisplayName("14. Calculate monthly trend")
    void testCalculateMonthlyTrend() throws Exception {
        LocalDate date1 = LocalDate.of(2026, 7, 10);
        LocalDate date2 = LocalDate.of(2026, 8, 15);
        LocalDate date3 = LocalDate.of(2026, 9, 20);

        mockMvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new ExpenseRequest(userId, categoryId, new BigDecimal("1000.00"), date1))));
        mockMvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new ExpenseRequest(userId, categoryId, new BigDecimal("1500.00"), date2))));
        mockMvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new ExpenseRequest(userId, categoryId, new BigDecimal("2000.00"), date3))));

        mockMvc.perform(get("/api/expenses/trend?userId=" + userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId", is(userId.intValue())))
            .andExpect(jsonPath("$.monthlySpending", hasSize(3)))
            .andExpect(jsonPath("$.monthlySpending[0].month", is("2026-07")))
            .andExpect(jsonPath("$.monthlySpending[0].total", is(1000.00)))
            .andExpect(jsonPath("$.monthlySpending[1].month", is("2026-08")))
            .andExpect(jsonPath("$.monthlySpending[1].total", is(1500.00)))
            .andExpect(jsonPath("$.monthlySpending[2].month", is("2026-09")))
            .andExpect(jsonPath("$.monthlySpending[2].total", is(2000.00)));
    }

    // 15. Calculate budget status
    @Test
    @Order(15)
    @DisplayName("15. Calculate budget status")
    void testCalculateBudgetStatus() throws Exception {
        BudgetRequest budgetReq = new BudgetRequest(userId, categoryId, new BigDecimal("5000.00"), "2026-09");
        mockMvc.perform(post("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(budgetReq)))
            .andExpect(status().isCreated());

        ExpenseRequest expReq = new ExpenseRequest(userId, categoryId, new BigDecimal("4200.00"), LocalDate.of(2026, 9, 10));
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expReq)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/budgets/status?userId=" + userId + "&categoryId=" + categoryId + "&month=2026-09"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.budgetAmount", is(5000.00)))
            .andExpect(jsonPath("$.totalSpent", is(4200.00)))
            .andExpect(jsonPath("$.remainingBudget", is(800.00)))
            .andExpect(jsonPath("$.percentageUsed", is(84.0)))
            .andExpect(jsonPath("$.alertLevel", is("NONE")));
    }

    // 16. Expense amount = 0
    @Test
    @Order(16)
    @DisplayName("16. Expense amount = 0 rejected")
    void testExpenseAmountZeroRejected() throws Exception {
        ExpenseRequest request = new ExpenseRequest(userId, categoryId, BigDecimal.ZERO, LocalDate.now());
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", notNullValue()));
    }

    // 17. Expense amount < 0
    @Test
    @Order(17)
    @DisplayName("17. Expense amount < 0 rejected")
    void testExpenseAmountNegativeRejected() throws Exception {
        ExpenseRequest request = new ExpenseRequest(userId, categoryId, new BigDecimal("-100.00"), LocalDate.now());
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", notNullValue()));
    }

    // 18. Budget amount = 0
    @Test
    @Order(18)
    @DisplayName("18. Budget amount = 0 rejected")
    void testBudgetAmountZeroRejected() throws Exception {
        BudgetRequest request = new BudgetRequest(userId, categoryId, BigDecimal.ZERO, "2026-09");
        mockMvc.perform(post("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", notNullValue()));
    }

    // 19. Budget amount < 0
    @Test
    @Order(19)
    @DisplayName("19. Budget amount < 0 rejected")
    void testBudgetAmountNegativeRejected() throws Exception {
        BudgetRequest request = new BudgetRequest(userId, categoryId, new BigDecimal("-500.00"), "2026-09");
        mockMvc.perform(post("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", notNullValue()));
    }

    // 20. Non-existing user
    @Test
    @Order(20)
    @DisplayName("20. Non-existing user rejected")
    void testNonExistingUserRejected() throws Exception {
        ExpenseRequest request = new ExpenseRequest(9999L, categoryId, new BigDecimal("100.00"), LocalDate.now());
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", is("User not found with id: 9999")));
    }

    // 21. Non-existing category
    @Test
    @Order(21)
    @DisplayName("21. Non-existing category rejected")
    void testNonExistingCategoryRejected() throws Exception {
        ExpenseRequest request = new ExpenseRequest(userId, 9999L, new BigDecimal("100.00"), LocalDate.now());
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", is("Category not found with id: 9999")));
    }

    // 22. Non-existing expense
    @Test
    @Order(22)
    @DisplayName("22. Non-existing expense returns 404")
    void testNonExistingExpense() throws Exception {
        mockMvc.perform(get("/api/expenses/9999"))
            .andExpect(status().isNotFound());
    }

    // 23. Non-existing budget
    @Test
    @Order(23)
    @DisplayName("23. Non-existing budget returns 404")
    void testNonExistingBudget() throws Exception {
        mockMvc.perform(get("/api/budgets/9999"))
            .andExpect(status().isNotFound());
    }

    // 24. Duplicate budget
    @Test
    @Order(24)
    @DisplayName("24. Duplicate budget rejected with 409 Conflict")
    void testDuplicateBudgetRejected() throws Exception {
        BudgetRequest request = new BudgetRequest(userId, categoryId, new BigDecimal("5000.00"), "2026-09");
        mockMvc.perform(post("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        // Attempt duplicate
        mockMvc.perform(post("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error", is("Duplicate Resource")));
    }

    // 25. Spending below 90%
    @Test
    @Order(25)
    @DisplayName("25. Spending below 90% produces no warning")
    void testSpendingBelow90Percent() throws Exception {
        BudgetRequest bReq = new BudgetRequest(userId, categoryId, new BigDecimal("1000.00"), "2026-09");
        mockMvc.perform(post("/api/budgets").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(bReq)))
            .andExpect(status().isCreated());

        ExpenseRequest eReq = new ExpenseRequest(userId, categoryId, new BigDecimal("800.00"), LocalDate.of(2026, 9, 5));
        mockMvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(eReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.budgetAlert.alertLevel", is("NONE")))
            .andExpect(jsonPath("$.budgetAlert.warning", is(false)))
            .andExpect(jsonPath("$.budgetAlert.percentageUsed", is(80.0)));
    }

    // 26. Spending exactly at 90%
    @Test
    @Order(26)
    @DisplayName("26. Spending exactly at 90% triggers WARNING")
    void testSpendingExactlyAt90Percent() throws Exception {
        BudgetRequest bReq = new BudgetRequest(userId, categoryId, new BigDecimal("1000.00"), "2026-09");
        mockMvc.perform(post("/api/budgets").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(bReq)))
            .andExpect(status().isCreated());

        ExpenseRequest eReq = new ExpenseRequest(userId, categoryId, new BigDecimal("900.00"), LocalDate.of(2026, 9, 5));
        mockMvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(eReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.budgetAlert.alertLevel", is("WARNING")))
            .andExpect(jsonPath("$.budgetAlert.warning", is(true)))
            .andExpect(jsonPath("$.budgetAlert.percentageUsed", is(90.0)))
            .andExpect(jsonPath("$.warningMessage", notNullValue()));
    }

    // 27. Spending above 90%
    @Test
    @Order(27)
    @DisplayName("27. Spending above 90% triggers WARNING")
    void testSpendingAbove90Percent() throws Exception {
        BudgetRequest bReq = new BudgetRequest(userId, categoryId, new BigDecimal("5000.00"), "2026-09");
        mockMvc.perform(post("/api/budgets").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(bReq)))
            .andExpect(status().isCreated());

        ExpenseRequest eReq = new ExpenseRequest(userId, categoryId, new BigDecimal("4600.00"), LocalDate.of(2026, 9, 5));
        mockMvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(eReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.budgetAlert.alertLevel", is("WARNING")))
            .andExpect(jsonPath("$.budgetAlert.warning", is(true)))
            .andExpect(jsonPath("$.budgetAlert.percentageUsed", is(92.0)))
            .andExpect(jsonPath("$.warningMessage", is("Warning: Food spending has reached 92.0% of the monthly budget.")));
    }

    // 28. Spending exactly at 100%
    @Test
    @Order(28)
    @DisplayName("28. Spending exactly at 100% triggers EXCEEDED")
    void testSpendingExactlyAt100Percent() throws Exception {
        BudgetRequest bReq = new BudgetRequest(userId, categoryId, new BigDecimal("1000.00"), "2026-09");
        mockMvc.perform(post("/api/budgets").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(bReq)))
            .andExpect(status().isCreated());

        ExpenseRequest eReq = new ExpenseRequest(userId, categoryId, new BigDecimal("1000.00"), LocalDate.of(2026, 9, 5));
        mockMvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(eReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.budgetAlert.alertLevel", is("EXCEEDED")))
            .andExpect(jsonPath("$.budgetAlert.warning", is(true)))
            .andExpect(jsonPath("$.budgetAlert.percentageUsed", is(100.0)))
            .andExpect(jsonPath("$.warningMessage", notNullValue()));
    }

    // 29. Spending above 100%
    @Test
    @Order(29)
    @DisplayName("29. Spending above 100% triggers EXCEEDED and still saves expense")
    void testSpendingAbove100Percent() throws Exception {
        BudgetRequest bReq = new BudgetRequest(userId, categoryId, new BigDecimal("1000.00"), "2026-09");
        mockMvc.perform(post("/api/budgets").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(bReq)))
            .andExpect(status().isCreated());

        ExpenseRequest eReq = new ExpenseRequest(userId, categoryId, new BigDecimal("1200.00"), LocalDate.of(2026, 9, 5));
        mockMvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(eReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.budgetAlert.alertLevel", is("EXCEEDED")))
            .andExpect(jsonPath("$.budgetAlert.warning", is(true)))
            .andExpect(jsonPath("$.budgetAlert.percentageUsed", is(120.0)));
    }

    // 30. Invalid email
    @Test
    @Order(30)
    @DisplayName("30. Invalid email rejected with 400")
    void testInvalidEmailRejected() throws Exception {
        UserRequest request = new UserRequest("Alex", "not-a-valid-email");
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.email", notNullValue()));
    }

    // 31. Blank category
    @Test
    @Order(31)
    @DisplayName("31. Blank category rejected with 400")
    void testBlankCategoryRejected() throws Exception {
        CategoryRequest request = new CategoryRequest("   ", "Empty");
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.name", notNullValue()));
    }

    // 32. Blank user name
    @Test
    @Order(32)
    @DisplayName("32. Blank user name rejected with 400")
    void testBlankUserNameRejected() throws Exception {
        UserRequest request = new UserRequest("   ", "valid@mail.com");
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.name", notNullValue()));
    }

    // 33. Missing required fields
    @Test
    @Order(33)
    @DisplayName("33. Missing required fields rejected with 400")
    void testMissingRequiredFieldsRejected() throws Exception {
        // Missing amount and date
        String incompleteJson = "{\"userId\": " + userId + ", \"categoryId\": " + categoryId + "}";
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(incompleteJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors", notNullValue()));
    }

    // Additional: Dashboard test
    @Test
    @Order(34)
    @DisplayName("34. Dashboard returns total counts")
    void testDashboard() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalUsers", greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.totalCategories", greaterThanOrEqualTo(1)));
    }

    // Additional: Expense Filtering test
    @Test
    @Order(35)
    @DisplayName("35. Expense filtering by user and category")
    void testExpenseFiltering() throws Exception {
        ExpenseRequest eReq = new ExpenseRequest(userId, categoryId, new BigDecimal("350.00"), LocalDate.of(2026, 9, 15));
        mockMvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(eReq)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/expenses?userId=" + userId + "&categoryId=" + categoryId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].amount", is(350.00)));
    }
}
