package com.example.expensetracker.repository;

import com.example.expensetracker.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByUserIdCode(String userIdCode);

    boolean existsByUserIdCode(String userIdCode);
}
