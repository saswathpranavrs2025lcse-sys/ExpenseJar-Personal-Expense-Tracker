package com.example.expensetracker.controller;

import com.example.expensetracker.dto.BudgetRequest;
import com.example.expensetracker.dto.BudgetResponse;
import com.example.expensetracker.dto.BudgetStatusResponse;
import com.example.expensetracker.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/budgets")
@Tag(name = "Budget Management", description = "Endpoints for creating and tracking category budgets and alerts")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    @Operation(
        summary = "Set a monthly budget for a category",
        description = "Creates a monthly budget limit. Rejects duplicate budgets for the same user, category, and month."
    )
    public ResponseEntity<BudgetResponse> createBudget(@Valid @RequestBody BudgetRequest request) {
        BudgetResponse response = budgetService.createBudget(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
        summary = "Get budgets with filters",
        description = "Retrieves budgets with optional filters for user, category, or month"
    )
    public ResponseEntity<List<BudgetResponse>> getAllBudgets(
        @Parameter(description = "Optional filter by user ID")
        @RequestParam(required = false) Long userId,
        @Parameter(description = "Optional filter by category ID")
        @RequestParam(required = false) Long categoryId,
        @Parameter(description = "Optional filter by month in YYYY-MM format")
        @RequestParam(required = false) String month
    ) {
        return ResponseEntity.ok(budgetService.getAllBudgets(userId, categoryId, month));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get budget by ID", description = "Retrieves budget details by its unique identifier")
    public ResponseEntity<BudgetResponse> getBudgetById(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getBudgetById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update budget", description = "Updates an existing budget amount or properties")
    public ResponseEntity<BudgetResponse> updateBudget(
        @PathVariable Long id,
        @Valid @RequestBody BudgetRequest request
    ) {
        return ResponseEntity.ok(budgetService.updateBudget(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete budget", description = "Deletes a budget record by its ID")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status")
    @Operation(
        summary = "Check budget status and alert",
        description = "Calculates spending status for a specific user, category, and month against the configured budget"
    )
    public ResponseEntity<BudgetStatusResponse> getBudgetStatus(
        @Parameter(description = "User ID", required = true)
        @RequestParam Long userId,
        @Parameter(description = "Category ID", required = true)
        @RequestParam Long categoryId,
        @Parameter(description = "Month in YYYY-MM format (e.g. 2026-09)", required = true)
        @RequestParam String month
    ) {
        return ResponseEntity.ok(budgetService.getBudgetStatus(userId, categoryId, month));
    }
}
