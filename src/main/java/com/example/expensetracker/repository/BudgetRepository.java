package com.example.expensetracker.repository;

import com.example.expensetracker.entity.Budget;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserId(Long userId);

    List<Budget> findByUserIdAndMonth(Long userId, String month);

    Optional<Budget> findByUserIdAndCategoryIdAndMonth(Long userId, Long categoryId, String month);

    boolean existsByUserIdAndCategoryIdAndMonth(Long userId, Long categoryId, String month);

    boolean existsByUserIdAndCategoryIdAndMonthAndIdNot(Long userId, Long categoryId, String month, Long id);

    List<Budget> findByCategoryId(Long categoryId);

    @Query("SELECT b FROM Budget b WHERE (:userId IS NULL OR b.user.id = :userId) " +
           "AND (:categoryId IS NULL OR b.category.id = :categoryId) " +
           "AND (:month IS NULL OR b.month = :month)")
    List<Budget> findWithFilters(
        @Param("userId") Long userId,
        @Param("categoryId") Long categoryId,
        @Param("month") String month
    );
}
