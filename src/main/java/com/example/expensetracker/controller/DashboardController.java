package com.example.expensetracker.controller;

import com.example.expensetracker.dto.DashboardResponse;
import com.example.expensetracker.dto.PersonalDashboardResponse;
import com.example.expensetracker.security.SecurityUtils;
import com.example.expensetracker.service.ExpenseReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Personal & System Dashboard", description = "Endpoints for user personal financial dashboard and high-level system metrics")
public class DashboardController {

    private final ExpenseReportService expenseReportService;

    public DashboardController(ExpenseReportService expenseReportService) {
        this.expenseReportService = expenseReportService;
    }

    @GetMapping("/personal")
    @Operation(summary = "Get personal financial dashboard", description = "Retrieves user-specific monthly metrics: total spent, monthly budget, remaining balance, spending insights, and recent expenses.")
    public ResponseEntity<PersonalDashboardResponse> getPersonalDashboard(
        @Parameter(description = "Optional user ID (defaults to authenticated user)")
        @RequestParam(required = false) Long userId,
        @Parameter(description = "Target month in YYYY-MM format (e.g. 2026-09)")
        @RequestParam(required = false) String month
    ) {
        Long effectiveUserId = SecurityUtils.getCurrentUserId().orElse(userId);
        if (effectiveUserId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(expenseReportService.getPersonalDashboard(effectiveUserId, month));
    }

    @GetMapping
    @Operation(summary = "Get system dashboard metrics", description = "Returns total counts of users, categories, expenses, budgets, and all-time total spending")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(expenseReportService.getDashboard());
    }
}
