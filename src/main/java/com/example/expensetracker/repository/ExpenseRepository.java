package com.example.expensetracker.repository;

import com.example.expensetracker.entity.Expense;
import com.example.expensetracker.repository.projection.CategorySpendingProjection;
import com.example.expensetracker.repository.projection.MonthlySpendingProjection;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserId(Long userId);

    List<Expense> findByCategoryId(Long categoryId);

    List<Expense> findTop10ByUserIdOrderByDateDescIdDesc(Long userId);

    @Query("SELECT e FROM Expense e WHERE (:userId IS NULL OR e.user.id = :userId) " +
           "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
           "AND (:startDate IS NULL OR e.date >= :startDate) " +
           "AND (:endDate IS NULL OR e.date <= :endDate) " +
           "ORDER BY e.date DESC, e.id DESC")
    List<Expense> findWithFilters(
        @Param("userId") Long userId,
        @Param("categoryId") Long categoryId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT e FROM Expense e WHERE (:userId IS NULL OR e.user.id = :userId) " +
           "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
           "AND (:startDate IS NULL OR e.date >= :startDate) " +
           "AND (:endDate IS NULL OR e.date <= :endDate) " +
           "AND (:minAmount IS NULL OR e.amount >= :minAmount) " +
           "AND (:maxAmount IS NULL OR e.amount <= :maxAmount) " +
           "AND (:search IS NULL OR LOWER(COALESCE(e.description, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.category.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY e.date DESC, e.id DESC")
    List<Expense> findWithAdvancedFilters(
        @Param("userId") Long userId,
        @Param("categoryId") Long categoryId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount,
        @Param("search") String search
    );

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user.id = :userId AND e.category.id = :categoryId " +
           "AND e.date BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalSpending(
        @Param("userId") Long userId,
        @Param("categoryId") Long categoryId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.date BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalSpendingForUserInPeriod(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT e.category.id AS categoryId, e.category.name AS categoryName, SUM(e.amount) AS totalSpent " +
           "FROM Expense e WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate " +
           "GROUP BY e.category.id, e.category.name ORDER BY totalSpent DESC")
    List<CategorySpendingProjection> findCategorySpendingBreakdown(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT YEAR(e.date) AS expenseYear, MONTH(e.date) AS expenseMonth, SUM(e.amount) AS total " +
           "FROM Expense e WHERE e.user.id = :userId " +
           "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
           "GROUP BY YEAR(e.date), MONTH(e.date) " +
           "ORDER BY YEAR(e.date) ASC, MONTH(e.date) ASC")
    List<MonthlySpendingProjection> findMonthlySpendingTrend(
        @Param("userId") Long userId,
        @Param("categoryId") Long categoryId
    );
}
