package com.fooddelivery.adminservice.repository;

import com.fooddelivery.adminservice.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findByStatus(String status);

    @Query("SELECT COUNT(s) FROM SupportTicket s WHERE s.status IN ('OPEN', 'IN_PROGRESS')")
    Long countPendingTickets();
}
