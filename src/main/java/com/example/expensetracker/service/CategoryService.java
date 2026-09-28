package com.example.expensetracker.service;

import com.example.expensetracker.dto.CategoryRequest;
import com.example.expensetracker.dto.CategoryResponse;
import com.example.expensetracker.entity.Category;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.exception.DuplicateResourceException;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.SecurityUtils;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        String trimmedName = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new DuplicateResourceException("Category already exists with name: " + trimmedName);
        }

        Category category = new Category();
        category.setName(trimmedName);
        category.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        SecurityUtils.getCurrentUserId().ifPresent(uid -> {
            Optional<User> userOpt = userRepository.findById(uid);
            userOpt.ifPresent(category::setUser);
        });

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        return mapToResponse(getCategoryEntityById(id));
    }

    @Transactional(readOnly = true)
    public Category getCategoryEntityById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories(String nameFilter) {
        Optional<Long> uidOpt = SecurityUtils.getCurrentUserId();
        List<Category> categories;

        if (uidOpt.isPresent()) {
            Long uid = uidOpt.get();
            if (nameFilter != null && !nameFilter.trim().isEmpty()) {
                categories = categoryRepository.findAvailableForUserByNameContaining(uid, nameFilter.trim());
            } else {
                categories = categoryRepository.findAllAvailableForUser(uid);
            }
        } else {
            if (nameFilter != null && !nameFilter.trim().isEmpty()) {
                categories = categoryRepository.findByNameContainingIgnoreCase(nameFilter.trim());
            } else {
                categories = categoryRepository.findAll();
            }
        }

        return categories.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}
