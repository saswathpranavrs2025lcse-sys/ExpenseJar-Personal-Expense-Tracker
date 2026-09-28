package com.example.expensetracker.controller;

import com.example.expensetracker.dto.ExpenseRequest;
import com.example.expensetracker.dto.ExpenseResponse;
import com.example.expensetracker.dto.ExpenseSummaryResponse;
import com.example.expensetracker.dto.SpendingTrendResponse;
import com.example.expensetracker.security.SecurityUtils;
import com.example.expensetracker.service.ExpenseReportService;
import com.example.expensetracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@RequestMapping("/api/expenses")
@Tag(name = "Expense Management & Reporting", description = "Endpoints for logging expenses, filtering, monthly summaries, and spending trends")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ExpenseReportService expenseReportService;

    public ExpenseController(ExpenseService expenseService, ExpenseReportService expenseReportService) {
        this.expenseService = expenseService;
        this.expenseReportService = expenseReportService;
    }

    @PostMapping
    @Operation(
        summary = "Add an expense",
        description = "Logs a new expense with amount, category, date, and user. Recalculates category budget immediately and triggers 90% or exceeded budget warning if applicable."
    )
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.createExpense(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
        summary = "Get expenses with filters",
        description = "Retrieves expenses with optional filtering by user, category, date range, amount range, and search text"
    )
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses(
        @Parameter(description = "Optional filter by user ID")
        @RequestParam(required = false) Long userId,
        @Parameter(description = "Optional filter by category ID")
        @RequestParam(required = false) Long categoryId,
        @Parameter(description = "Optional filter by month (YYYY-MM)")
        @RequestParam(required = false) String month,
        @Parameter(description = "Optional filter by start date (YYYY-MM-DD)")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @Parameter(description = "Optional filter by end date (YYYY-MM-DD)")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @Parameter(description = "Optional filter by minimum amount")
        @RequestParam(required = false) BigDecimal minAmount,
        @Parameter(description = "Optional filter by maximum amount")
        @RequestParam(required = false) BigDecimal maxAmount,
        @Parameter(description = "Optional text search on description or category name")
        @RequestParam(required = false) String search
    ) {
        LocalDate effectiveStart = startDate;
        LocalDate effectiveEnd = endDate;

        if (month != null && !month.trim().isEmpty() && effectiveStart == null && effectiveEnd == null) {
            YearMonth ym = YearMonth.parse(month.trim());
            effectiveStart = ym.atDay(1);
            effectiveEnd = ym.atEndOfMonth();
        }

        return ResponseEntity.ok(expenseService.getAllExpenses(userId, categoryId, effectiveStart, effectiveEnd, minAmount, maxAmount, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get expense by ID", description = "Retrieves an expense by its unique identifier")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an expense", description = "Updates an existing expense and recalculates budget alert status")
    public ResponseEntity<ExpenseResponse> updateExpense(
        @PathVariable Long id,
        @Valid @RequestBody ExpenseRequest request
    ) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense", description = "Deletes an expense record by its ID")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Export expenses to CSV", description = "Generates and downloads a clean CSV export of the user's filtered expenses")
    public ResponseEntity<byte[]> exportExpensesCsv(
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String month,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) BigDecimal minAmount,
        @RequestParam(required = false) BigDecimal maxAmount,
        @RequestParam(required = false) String search
    ) {
        LocalDate effectiveStart = startDate;
        LocalDate effectiveEnd = endDate;
        if (month != null && !month.trim().isEmpty() && effectiveStart == null && effectiveEnd == null) {
            YearMonth ym = YearMonth.parse(month.trim());
            effectiveStart = ym.atDay(1);
            effectiveEnd = ym.atEndOfMonth();
        }

        byte[] csvBytes = expenseService.exportExpensesToCsv(userId, categoryId, effectiveStart, effectiveEnd, minAmount, maxAmount, search);

        String filename = "expenses_export_" + (month != null ? month : LocalDate.now().toString()) + ".csv";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(csvBytes);
    }

    @GetMapping("/summary/current")
    @Operation(
        summary = "View total spending per category for current or specified month",
        description = "Shows total spending per category for current month or specified month against monthly budgets with remaining balance and percentage used"
    )
    public ResponseEntity<List<ExpenseSummaryResponse>> getCurrentMonthSummary(
        @Parameter(description = "User ID", required = false)
        @RequestParam(required = false) Long userId,
        @Parameter(description = "Optional Month in YYYY-MM format (e.g. 2026-09)")
        @RequestParam(required = false) String month
    ) {
        Long effectiveUserId = SecurityUtils.getCurrentUserId().orElse(userId);
        if (effectiveUserId == null) {
            return ResponseEntity.badRequest().build();
        }

        if (month != null && !month.trim().isEmpty()) {
            return ResponseEntity.ok(expenseReportService.getMonthCategorySummaries(effectiveUserId, YearMonth.parse(month.trim())));
        }
        return ResponseEntity.ok(expenseReportService.getCurrentMonthCategorySummaries(effectiveUserId));
    }

    @GetMapping("/summary")
    @Operation(
        summary = "View spending summary for a category and month",
        description = "Shows total spent, budget, remaining amount, and percentage used for a specific user, category, and month"
    )
    public ResponseEntity<ExpenseSummaryResponse> getCategorySummary(
        @Parameter(description = "User ID", required = false)
        @RequestParam(required = false) Long userId,
        @Parameter(description = "Category ID", required = true)
        @RequestParam Long categoryId,
        @Parameter(description = "Month in YYYY-MM format (e.g. 2026-09)", required = true)
        @RequestParam String month
    ) {
        Long effectiveUserId = SecurityUtils.getCurrentUserId().orElse(userId);
        return ResponseEntity.ok(expenseReportService.getCategorySummary(effectiveUserId, categoryId, month));
    }

    @GetMapping("/trend")
    @Operation(
        summary = "View month-over-month spending trend",
        description = "Calculates month-over-month spending trends across recorded months from the database"
    )
    public ResponseEntity<SpendingTrendResponse> getSpendingTrend(
        @Parameter(description = "User ID", required = false)
        @RequestParam(required = false) Long userId,
        @Parameter(description = "Optional Category ID to inspect category-specific trend")
        @RequestParam(required = false) Long categoryId
    ) {
        Long effectiveUserId = SecurityUtils.getCurrentUserId().orElse(userId);
        return ResponseEntity.ok(expenseReportService.getSpendingTrend(effectiveUserId, categoryId));
    }
}
