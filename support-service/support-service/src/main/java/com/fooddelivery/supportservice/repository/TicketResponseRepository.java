package com.fooddelivery.supportservice.repository;

import com.fooddelivery.supportservice.entity.TicketResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketResponseRepository extends JpaRepository<TicketResponse, Long> {

    List<TicketResponse> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    List<TicketResponse> findByTicketIdAndIsInternalNoteFalseOrderByCreatedAtAsc(Long ticketId);
}
