package com.fooddelivery.supportservice.service;

import com.fooddelivery.supportservice.dto.*;
import com.fooddelivery.supportservice.entity.SupportTicket;
import com.fooddelivery.supportservice.entity.TicketResponse;
import com.fooddelivery.supportservice.enums.TicketStatus;
import com.fooddelivery.supportservice.repository.SupportTicketRepository;
import com.fooddelivery.supportservice.repository.TicketResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportService {

    private final SupportTicketRepository ticketRepository;
    private final TicketResponseRepository responseRepository;

    // Create Ticket
    @Transactional
    public SupportTicketResponse createTicket(CreateTicketRequest request) {
        SupportTicket ticket = new SupportTicket();
        ticket.setTicketNumber(generateTicketNumber());
        ticket.setUserId(request.getUserId());
        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory());
        ticket.setPriority(request.getPriority() != null ? request.getPriority() : com.fooddelivery.supportservice.enums.TicketPriority.MEDIUM);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setOrderId(request.getOrderId());

        SupportTicket saved = ticketRepository.save(ticket);
        return mapToTicketResponse(saved);
    }

    // Get Ticket by ID
    public SupportTicketResponse getTicket(Long id) {
        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
        return mapToTicketResponse(ticket);
    }

    // Get Ticket by Ticket Number
    public SupportTicketResponse getTicketByNumber(String ticketNumber) {
        SupportTicket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new RuntimeException("Ticket not found with number: " + ticketNumber));
        return mapToTicketResponse(ticket);
    }

    // Get Tickets by User
    public List<SupportTicketResponse> getTicketsByUser(Long userId) {
        List<SupportTicket> tickets = ticketRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return tickets.stream()
                .map(this::mapToTicketResponse)
                .collect(Collectors.toList());
    }

    // Get Tickets by Status
    public List<SupportTicketResponse> getTicketsByStatus(TicketStatus status) {
        List<SupportTicket> tickets = ticketRepository.findByStatusOrderByCreatedAtDesc(status);
        return tickets.stream()
                .map(this::mapToTicketResponse)
                .collect(Collectors.toList());
    }

    // Get Tickets by Agent
    public List<SupportTicketResponse> getTicketsByAgent(Long agentId) {
        List<SupportTicket> tickets = ticketRepository.findByAssignedToOrderByCreatedAtDesc(agentId);
        return tickets.stream()
                .map(this::mapToTicketResponse)
                .collect(Collectors.toList());
    }

    // Get Tickets by Order
    public List<SupportTicketResponse> getTicketsByOrder(Long orderId) {
        List<SupportTicket> tickets = ticketRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
        return tickets.stream()
                .map(this::mapToTicketResponse)
                .collect(Collectors.toList());
    }

    // Update Ticket Status
    @Transactional
    public SupportTicketResponse updateTicketStatus(Long id, UpdateTicketStatusRequest request) {
        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));

        ticket.setStatus(request.getStatus());

        if (request.getStatus() == TicketStatus.CLOSED) {
            ticket.setClosedAt(LocalDateTime.now());
        } else if (request.getStatus() == TicketStatus.REOPENED) {
            ticket.setClosedAt(null);
        }

        SupportTicket updated = ticketRepository.save(ticket);
        return mapToTicketResponse(updated);
    }

    // Update Ticket Priority
    @Transactional
    public SupportTicketResponse updateTicketPriority(Long id, UpdateTicketPriorityRequest request) {
        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));

        ticket.setPriority(request.getPriority());

        SupportTicket updated = ticketRepository.save(ticket);
        return mapToTicketResponse(updated);
    }

    // Assign Ticket to Agent
    @Transactional
    public SupportTicketResponse assignTicket(Long id, AssignTicketRequest request) {
        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));

        ticket.setAssignedTo(request.getAgentId());

        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        SupportTicket updated = ticketRepository.save(ticket);
        return mapToTicketResponse(updated);
    }

    // Add Response to Ticket
    @Transactional
    public TicketResponseDto addResponse(Long ticketId, AddResponseRequest request) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        TicketResponse response = new TicketResponse();
        response.setTicketId(ticketId);
        response.setResponderId(request.getResponderId());
        response.setResponderType(request.getResponderType());
        response.setMessage(request.getMessage());
        response.setIsInternalNote(request.getIsInternalNote() != null ? request.getIsInternalNote() : false);

        // Update ticket status if user responded
        if (request.getResponderType() == com.fooddelivery.supportservice.enums.ResponderType.USER
                && ticket.getStatus() == TicketStatus.RESOLVED) {
            ticket.setStatus(TicketStatus.REOPENED);
            ticketRepository.save(ticket);
        }

        TicketResponse saved = responseRepository.save(response);
        return mapToResponseDto(saved);
    }

    // Get Ticket with Responses
    public TicketWithResponsesDto getTicketWithResponses(Long ticketId, boolean includeInternalNotes) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        List<TicketResponse> responses;
        if (includeInternalNotes) {
            responses = responseRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        } else {
            responses = responseRepository.findByTicketIdAndIsInternalNoteFalseOrderByCreatedAtAsc(ticketId);
        }

        TicketWithResponsesDto dto = new TicketWithResponsesDto();
        dto.setTicket(mapToTicketResponse(ticket));
        dto.setResponses(responses.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList()));

        return dto;
    }

    // Close Ticket
    @Transactional
    public SupportTicketResponse closeTicket(Long id) {
        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));

        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(LocalDateTime.now());

        SupportTicket updated = ticketRepository.save(ticket);
        return mapToTicketResponse(updated);
    }

    // Reopen Ticket
    @Transactional
    public SupportTicketResponse reopenTicket(Long id) {
        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));

        if (ticket.getStatus() != TicketStatus.CLOSED && ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new RuntimeException("Only closed or resolved tickets can be reopened");
        }

        ticket.setStatus(TicketStatus.REOPENED);
        ticket.setClosedAt(null);

        SupportTicket updated = ticketRepository.save(ticket);
        return mapToTicketResponse(updated);
    }

    // Generate Ticket Number
    private String generateTicketNumber() {
        String prefix = "TKT";
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String ticketNumber = prefix + "-" + uuid;

        // Ensure uniqueness
        while (ticketRepository.existsByTicketNumber(ticketNumber)) {
            uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            ticketNumber = prefix + "-" + uuid;
        }

        return ticketNumber;
    }

    // Helper: Map Ticket to Response
    private SupportTicketResponse mapToTicketResponse(SupportTicket ticket) {
        SupportTicketResponse response = new SupportTicketResponse();
        response.setId(ticket.getId());
        response.setTicketNumber(ticket.getTicketNumber());
        response.setUserId(ticket.getUserId());
        response.setSubject(ticket.getSubject());
        response.setDescription(ticket.getDescription());
        response.setCategory(ticket.getCategory());
        response.setPriority(ticket.getPriority());
        response.setStatus(ticket.getStatus());
        response.setAssignedTo(ticket.getAssignedTo());
        response.setOrderId(ticket.getOrderId());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());
        response.setClosedAt(ticket.getClosedAt());
        return response;
    }

    // Helper: Map Response to DTO
    private TicketResponseDto mapToResponseDto(TicketResponse response) {
        TicketResponseDto dto = new TicketResponseDto();
        dto.setId(response.getId());
        dto.setTicketId(response.getTicketId());
        dto.setResponderId(response.getResponderId());
        dto.setResponderType(response.getResponderType());
        dto.setMessage(response.getMessage());
        dto.setIsInternalNote(response.getIsInternalNote());
        dto.setCreatedAt(response.getCreatedAt());
        return dto;
    }
}
