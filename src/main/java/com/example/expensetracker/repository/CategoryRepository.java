package com.example.expensetracker.repository;

import com.example.expensetracker.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Optional<Category> findByNameIgnoreCase(String name);

    List<Category> findByNameContainingIgnoreCase(String name);

    @Query("SELECT c FROM Category c WHERE c.user IS NULL OR c.user.id = :userId ORDER BY c.name ASC")
    List<Category> findAllAvailableForUser(@Param("userId") Long userId);

    @Query("SELECT c FROM Category c WHERE (c.user IS NULL OR c.user.id = :userId) AND LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY c.name ASC")
    List<Category> findAvailableForUserByNameContaining(@Param("userId") Long userId, @Param("name") String name);
}
