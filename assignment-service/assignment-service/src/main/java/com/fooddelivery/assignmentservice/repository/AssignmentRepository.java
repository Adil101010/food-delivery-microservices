package com.fooddelivery.assignmentservice.repository;

import com.fooddelivery.assignmentservice.entity.Assignment;
import com.fooddelivery.assignmentservice.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    Optional<Assignment> findByOrderId(Long orderId);

    List<Assignment> findByPartnerId(Long partnerId);

    List<Assignment> findByCustomerId(Long customerId);

    List<Assignment> findByStatus(AssignmentStatus status);

    List<Assignment> findByPartnerIdAndStatus(Long partnerId, AssignmentStatus status);
}
