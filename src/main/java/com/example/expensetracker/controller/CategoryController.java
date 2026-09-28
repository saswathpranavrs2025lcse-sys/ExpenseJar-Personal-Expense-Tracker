package com.example.expensetracker.controller;

import com.example.expensetracker.dto.CategoryRequest;
import com.example.expensetracker.dto.CategoryResponse;
import com.example.expensetracker.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Category Management", description = "Endpoints for managing spending categories (Food, Travel, Stationery, etc.)")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @Operation(summary = "Create a category", description = "Creates a new category for grouping expenses and budgets")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get categories", description = "Retrieves all categories with optional search filtering by name")
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
        @Parameter(description = "Optional filter by category name")
        @RequestParam(required = false) String name
    ) {
        return ResponseEntity.ok(categoryService.getAllCategories(name));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieves details of a category by ID")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }
}
