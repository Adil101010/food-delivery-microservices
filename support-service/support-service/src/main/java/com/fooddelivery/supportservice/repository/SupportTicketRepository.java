package com.fooddelivery.supportservice.repository;

import com.fooddelivery.supportservice.entity.SupportTicket;
import com.fooddelivery.supportservice.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    Optional<SupportTicket> findByTicketNumber(String ticketNumber);

    List<SupportTicket> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<SupportTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status);

    List<SupportTicket> findByAssignedToOrderByCreatedAtDesc(Long assignedTo);

    List<SupportTicket> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    boolean existsByTicketNumber(String ticketNumber);
}
