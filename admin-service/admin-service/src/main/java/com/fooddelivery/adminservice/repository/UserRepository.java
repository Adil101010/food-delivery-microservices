package com.fooddelivery.adminservice.repository;

import com.fooddelivery.adminservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByRole(String role);

    List<User> findByIsActive(Boolean isActive);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    Long countNewUsers(LocalDateTime startDate);

    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    List<User> findAllOrderByCreatedAtDesc();
}
