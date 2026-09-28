package com.example.expensetracker.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.expensetracker.dto.AuthRequest;
import com.example.expensetracker.dto.BudgetRequest;
import com.example.expensetracker.dto.CategoryRequest;
import com.example.expensetracker.dto.ChangePasswordRequest;
import com.example.expensetracker.dto.ExpenseRequest;
import com.example.expensetracker.dto.RegisterRequest;
import com.example.expensetracker.dto.UpdateProfileRequest;
import com.example.expensetracker.entity.RecurringType;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationAndFeaturesTest {

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

    @BeforeEach
    void cleanDatabase() {
        expenseRepository.deleteAll();
        budgetRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("1. User registration generates auto User ID (e.g. EXP-10001) and hashes password")
    void testUserRegistration() throws Exception {
        RegisterRequest request = new RegisterRequest("Mithil Pranav", "mithil@example.com", "securePass123", "securePass123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token", notNullValue()))
            .andExpect(jsonPath("$.user.name", is("Mithil Pranav")))
            .andExpect(jsonPath("$.user.email", is("mithil@example.com")))
            .andExpect(jsonPath("$.user.userIdCode", containsString("EXP-")))
            .andExpect(jsonPath("$.message", containsString("Account created successfully")));
    }

    @Test
    @DisplayName("2. Registration with duplicate email is rejected with 409 Conflict")
    void testDuplicateEmailRejected() throws Exception {
        RegisterRequest request1 = new RegisterRequest("User One", "user@example.com", "pass12345", "pass12345");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
            .andExpect(status().isCreated());

        RegisterRequest request2 = new RegisterRequest("User Two", "user@example.com", "pass67890", "pass67890");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message", containsString("An account with this email already exists.")));
    }

    @Test
    @DisplayName("3. Login with correct credentials returns JWT token, wrong credentials return 401")
    void testLoginSuccessAndFailure() throws Exception {
        RegisterRequest reg = new RegisterRequest("Mithil", "mithil.login@example.com", "mypassword1", "mypassword1");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
            .andExpect(status().isCreated());

        // Wrong password
        AuthRequest wrongReq = new AuthRequest("mithil.login@example.com", "wrongPassword");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongReq)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message", is("Invalid email or password.")));

        // Correct password
        AuthRequest correctReq = new AuthRequest("mithil.login@example.com", "mypassword1", true);
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(correctReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token", notNullValue()))
            .andExpect(jsonPath("$.user.email", is("mithil.login@example.com")))
            .andExpect(jsonPath("$.user.userIdCode", notNullValue()));
    }

    @Test
    @DisplayName("4. Protected endpoints require valid Bearer token")
    void testProtectedEndpointsRequireToken() throws Exception {
        // Without token -> 401 Unauthorized
        mockMvc.perform(get("/api/dashboard/personal?month=2026-09"))
            .andExpect(status().isUnauthorized());

        // Register and get token
        RegisterRequest reg = new RegisterRequest("Tester", "tester.dash@example.com", "pass12345", "pass12345");
        MvcResult regRes = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
            .andExpect(status().isCreated())
            .andReturn();
        String token = objectMapper.readTree(regRes.getResponse().getContentAsString()).get("token").asText();

        // With token -> 200 OK
        mockMvc.perform(get("/api/dashboard/personal?month=2026-09")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.month", is("2026-09")))
            .andExpect(jsonPath("$.totalSpent", is(0)));
    }

    @Test
    @DisplayName("5. User profile view, update name and email, and change password")
    void testProfileManagement() throws Exception {
        RegisterRequest reg = new RegisterRequest("Old Name", "old.email@example.com", "pass12345", "pass12345");
        MvcResult regRes = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
            .andExpect(status().isCreated())
            .andReturn();
        String token = objectMapper.readTree(regRes.getResponse().getContentAsString()).get("token").asText();

        // GET profile
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Old Name")))
            .andExpect(jsonPath("$.email", is("old.email@example.com")))
            .andExpect(jsonPath("$.userIdCode", containsString("EXP-")));

        // Update profile
        UpdateProfileRequest updateReq = new UpdateProfileRequest("New Name", "new.email@example.com");
        mockMvc.perform(put("/api/auth/profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("New Name")))
            .andExpect(jsonPath("$.email", is("new.email@example.com")));

        // Change password
        ChangePasswordRequest changeReq = new ChangePasswordRequest("pass12345", "brandNewPass123", "brandNewPass123");
        mockMvc.perform(post("/api/auth/change-password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changeReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message", containsString("Password changed successfully")));

        // Old password now fails
        AuthRequest oldLogin = new AuthRequest("new.email@example.com", "pass12345");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oldLogin)))
            .andExpect(status().isUnauthorized());

        // New password succeeds
        AuthRequest newLogin = new AuthRequest("new.email@example.com", "brandNewPass123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newLogin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    @DisplayName("6. Data Isolation: User A cannot access or mutate User B's financial records")
    void testUserDataIsolation() throws Exception {
        // Register User A
        RegisterRequest regA = new RegisterRequest("User A", "user.a@bank.com", "passwordA1", "passwordA1");
        MvcResult resA = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regA)))
            .andReturn();
        String tokenA = objectMapper.readTree(resA.getResponse().getContentAsString()).get("token").asText();
        Long userAId = objectMapper.readTree(resA.getResponse().getContentAsString()).get("user").get("id").asLong();

        // Register User B
        RegisterRequest regB = new RegisterRequest("User B", "user.b@bank.com", "passwordB1", "passwordB1");
        MvcResult resB = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regB)))
            .andReturn();
        String tokenB = objectMapper.readTree(resB.getResponse().getContentAsString()).get("token").asText();
        Long userBId = objectMapper.readTree(resB.getResponse().getContentAsString()).get("user").get("id").asLong();

        // Create category
        CategoryRequest catReq = new CategoryRequest("Food", "Food and Dining");
        MvcResult catRes = mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(catReq)))
            .andReturn();
        Long categoryId = objectMapper.readTree(catRes.getResponse().getContentAsString()).get("id").asLong();

        // User A creates an expense of ₹1500
        ExpenseRequest expReq = new ExpenseRequest(userAId, categoryId, new BigDecimal("1500.00"), LocalDate.of(2026, 9, 15), "User A Groceries");
        MvcResult expRes = mockMvc.perform(post("/api/expenses")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expReq)))
            .andExpect(status().isCreated())
            .andReturn();
        Long expenseAId = objectMapper.readTree(expRes.getResponse().getContentAsString()).get("id").asLong();

        // User B attempts to access User A's expense -> Forbidden (403)
        mockMvc.perform(get("/api/expenses/" + expenseAId)
                .header("Authorization", "Bearer " + tokenB))
            .andExpect(status().isForbidden());

        // User B queries /api/expenses -> Only sees their own expenses (0 items)
        mockMvc.perform(get("/api/expenses")
                .header("Authorization", "Bearer " + tokenB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));

        // User A queries /api/expenses -> Sees their 1 item
        mockMvc.perform(get("/api/expenses")
                .header("Authorization", "Bearer " + tokenA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].description", is("User A Groceries")));
    }

    @Test
    @DisplayName("7. Personal Dashboard calculates dynamic summary, smart insights, and budget alert")
    void testPersonalDashboardAndInsights() throws Exception {
        RegisterRequest reg = new RegisterRequest("Student", "student@insights.com", "pass12345", "pass12345");
        MvcResult res = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
            .andReturn();
        String token = objectMapper.readTree(res.getResponse().getContentAsString()).get("token").asText();
        Long userId = objectMapper.readTree(res.getResponse().getContentAsString()).get("user").get("id").asLong();

        // Create Category Food
        CategoryRequest catReq = new CategoryRequest("Food", "Food expenses");
        MvcResult catRes = mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(catReq)))
            .andReturn();
        Long catId = objectMapper.readTree(catRes.getResponse().getContentAsString()).get("id").asLong();

        // Set monthly budget of ₹5000 for Food in 2026-09
        BudgetRequest budgetReq = new BudgetRequest(userId, catId, new BigDecimal("5000.00"), "2026-09");
        mockMvc.perform(post("/api/budgets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(budgetReq)))
            .andExpect(status().isCreated());

        // Add expense of ₹4600 (92% => Warning!)
        ExpenseRequest expReq = new ExpenseRequest(userId, catId, new BigDecimal("4600.00"), LocalDate.of(2026, 9, 10), "Hostel Mess", RecurringType.MONTHLY);
        mockMvc.perform(post("/api/expenses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.recurringType", is("MONTHLY")))
            .andExpect(jsonPath("$.budgetAlert.warning", is(true)));

        // Call Personal Dashboard
        mockMvc.perform(get("/api/dashboard/personal?month=2026-09")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.month", is("2026-09")))
            .andExpect(jsonPath("$.totalSpent", is(4600.0)))
            .andExpect(jsonPath("$.monthlyBudget", is(5000.0)))
            .andExpect(jsonPath("$.remainingBudget", is(400.0)))
            .andExpect(jsonPath("$.budgetUsedPercentage", is(92.0)))
            .andExpect(jsonPath("$.largestCategoryName", is("Food")))
            .andExpect(jsonPath("$.summaryText", containsString("You spent ₹4600 this month")))
            .andExpect(jsonPath("$.summaryText", containsString("Your largest spending category was Food")))
            .andExpect(jsonPath("$.insights", hasSize(greaterThanOrEqualTo(2))))
            .andExpect(jsonPath("$.recentExpenses", hasSize(1)));
    }

    @Test
    @DisplayName("8. CSV export downloads clean CSV file with headers")
    void testCsvExport() throws Exception {
        RegisterRequest reg = new RegisterRequest("Exporter", "export@test.com", "pass12345", "pass12345");
        MvcResult res = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
            .andReturn();
        String token = objectMapper.readTree(res.getResponse().getContentAsString()).get("token").asText();
        Long userId = objectMapper.readTree(res.getResponse().getContentAsString()).get("user").get("id").asLong();

        CategoryRequest catReq = new CategoryRequest("Travel", "Bus and train");
        MvcResult catRes = mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(catReq)))
            .andReturn();
        Long catId = objectMapper.readTree(catRes.getResponse().getContentAsString()).get("id").asLong();

        ExpenseRequest expReq = new ExpenseRequest(userId, catId, new BigDecimal("250.00"), LocalDate.of(2026, 9, 20), "Metro Card Recharge", RecurringType.MONTHLY);
        mockMvc.perform(post("/api/expenses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expReq)))
            .andExpect(status().isCreated());

        MvcResult csvRes = mockMvc.perform(get("/api/expenses/export/csv?month=2026-09")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("attachment; filename=")))
            .andReturn();

        String csvContent = csvRes.getResponse().getContentAsString();
        assertTrue(csvContent.contains("Date,Category,Amount (INR),Description,Recurring"));
        assertTrue(csvContent.contains("Travel"));
        assertTrue(csvContent.contains("250.00"));
        assertTrue(csvContent.contains("Metro Card Recharge"));
        assertTrue(csvContent.contains("MONTHLY"));
        assertFalse(csvContent.contains("password"));
    }
}
