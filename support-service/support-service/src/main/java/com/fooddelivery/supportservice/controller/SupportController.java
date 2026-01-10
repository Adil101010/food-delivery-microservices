package com.fooddelivery.supportservice.controller;

import com.fooddelivery.supportservice.dto.*;
import com.fooddelivery.supportservice.enums.TicketStatus;
import com.fooddelivery.supportservice.service.SupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SupportController {

    private final SupportService supportService;

    // Health Check
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("Support Service is running"));
    }

    // Create Ticket
    @PostMapping("/tickets")
    public ResponseEntity<SupportTicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request) {
        SupportTicketResponse ticket = supportService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    // Get Ticket by ID
    @GetMapping("/tickets/{id}")
    public ResponseEntity<SupportTicketResponse> getTicket(@PathVariable Long id) {
        SupportTicketResponse ticket = supportService.getTicket(id);
        return ResponseEntity.ok(ticket);
    }

    // Get Ticket by Ticket Number
    @GetMapping("/tickets/number/{ticketNumber}")
    public ResponseEntity<SupportTicketResponse> getTicketByNumber(@PathVariable String ticketNumber) {
        SupportTicketResponse ticket = supportService.getTicketByNumber(ticketNumber);
        return ResponseEntity.ok(ticket);
    }

    // Get Tickets by User
    @GetMapping("/tickets/user/{userId}")
    public ResponseEntity<List<SupportTicketResponse>> getTicketsByUser(@PathVariable Long userId) {
        List<SupportTicketResponse> tickets = supportService.getTicketsByUser(userId);
        return ResponseEntity.ok(tickets);
    }

    // Get Tickets by Status
    @GetMapping("/tickets/status/{status}")
    public ResponseEntity<List<SupportTicketResponse>> getTicketsByStatus(@PathVariable TicketStatus status) {
        List<SupportTicketResponse> tickets = supportService.getTicketsByStatus(status);
        return ResponseEntity.ok(tickets);
    }

    // Get Tickets by Agent
    @GetMapping("/tickets/agent/{agentId}")
    public ResponseEntity<List<SupportTicketResponse>> getTicketsByAgent(@PathVariable Long agentId) {
        List<SupportTicketResponse> tickets = supportService.getTicketsByAgent(agentId);
        return ResponseEntity.ok(tickets);
    }

    // Get Tickets by Order
    @GetMapping("/tickets/order/{orderId}")
    public ResponseEntity<List<SupportTicketResponse>> getTicketsByOrder(@PathVariable Long orderId) {
        List<SupportTicketResponse> tickets = supportService.getTicketsByOrder(orderId);
        return ResponseEntity.ok(tickets);
    }

    // Update Ticket Status
    @PutMapping("/tickets/{id}/status")
    public ResponseEntity<SupportTicketResponse> updateTicketStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketStatusRequest request) {
        SupportTicketResponse ticket = supportService.updateTicketStatus(id, request);
        return ResponseEntity.ok(ticket);
    }

    // Update Ticket Priority
    @PutMapping("/tickets/{id}/priority")
    public ResponseEntity<SupportTicketResponse> updateTicketPriority(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketPriorityRequest request) {
        SupportTicketResponse ticket = supportService.updateTicketPriority(id, request);
        return ResponseEntity.ok(ticket);
    }

    // Assign Ticket to Agent
    @PutMapping("/tickets/{id}/assign")
    public ResponseEntity<SupportTicketResponse> assignTicket(
            @PathVariable Long id,
            @Valid @RequestBody AssignTicketRequest request) {
        SupportTicketResponse ticket = supportService.assignTicket(id, request);
        return ResponseEntity.ok(ticket);
    }

    // Add Response to Ticket
    @PostMapping("/tickets/{ticketId}/responses")
    public ResponseEntity<TicketResponseDto> addResponse(
            @PathVariable Long ticketId,
            @Valid @RequestBody AddResponseRequest request) {
        TicketResponseDto response = supportService.addResponse(ticketId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get Ticket with Responses (Public - no internal notes)
    @GetMapping("/tickets/{ticketId}/with-responses")
    public ResponseEntity<TicketWithResponsesDto> getTicketWithResponses(@PathVariable Long ticketId) {
        TicketWithResponsesDto ticket = supportService.getTicketWithResponses(ticketId, false);
        return ResponseEntity.ok(ticket);
    }

    // Get Ticket with All Responses (Agent view - includes internal notes)
    @GetMapping("/tickets/{ticketId}/with-responses/all")
    public ResponseEntity<TicketWithResponsesDto> getTicketWithAllResponses(@PathVariable Long ticketId) {
        TicketWithResponsesDto ticket = supportService.getTicketWithResponses(ticketId, true);
        return ResponseEntity.ok(ticket);
    }

    // Close Ticket
    @PutMapping("/tickets/{id}/close")
    public ResponseEntity<SupportTicketResponse> closeTicket(@PathVariable Long id) {
        SupportTicketResponse ticket = supportService.closeTicket(id);
        return ResponseEntity.ok(ticket);
    }

    // Reopen Ticket
    @PutMapping("/tickets/{id}/reopen")
    public ResponseEntity<SupportTicketResponse> reopenTicket(@PathVariable Long id) {
        SupportTicketResponse ticket = supportService.reopenTicket(id);
        return ResponseEntity.ok(ticket);
    }
}
