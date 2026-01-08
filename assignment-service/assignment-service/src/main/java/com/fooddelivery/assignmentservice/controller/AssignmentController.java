package com.fooddelivery.assignmentservice.controller;

import com.fooddelivery.assignmentservice.dto.*;
import com.fooddelivery.assignmentservice.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AssignmentController {

    private final AssignmentService assignmentService;

    // Health Check
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("Assignment Service is running"));
    }

    // Auto Assignment (Find nearest partner)
    @PostMapping("/auto")
    public ResponseEntity<AssignmentResponse> autoAssign(
            @Valid @RequestBody AutoAssignRequest request) {
        AssignmentResponse assignment = assignmentService.autoAssign(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
    }

    // Manual Assignment (Admin override)
    @PostMapping("/manual")
    public ResponseEntity<AssignmentResponse> manualAssign(
            @Valid @RequestBody ManualAssignRequest request) {
        AssignmentResponse assignment = assignmentService.manualAssign(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
    }

    // Accept Assignment
    @PostMapping("/{assignmentId}/accept")
    public ResponseEntity<AssignmentResponse> acceptAssignment(@PathVariable Long assignmentId) {
        AssignmentResponse assignment = assignmentService.acceptAssignment(assignmentId);
        return ResponseEntity.ok(assignment);
    }

    // Reject Assignment
    @PostMapping("/{assignmentId}/reject")
    public ResponseEntity<AssignmentResponse> rejectAssignment(
            @PathVariable Long assignmentId,
            @RequestParam String reason) {
        AssignmentResponse assignment = assignmentService.rejectAssignment(assignmentId, reason);
        return ResponseEntity.ok(assignment);
    }

    // Get Assignment by Order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<AssignmentResponse> getAssignmentByOrderId(@PathVariable Long orderId) {
        AssignmentResponse assignment = assignmentService.getAssignmentByOrderId(orderId);
        return ResponseEntity.ok(assignment);
    }

    // Get Partner Assignments
    @GetMapping("/partner/{partnerId}")
    public ResponseEntity<List<AssignmentResponse>> getPartnerAssignments(@PathVariable Long partnerId) {
        List<AssignmentResponse> assignments = assignmentService.getPartnerAssignments(partnerId);
        return ResponseEntity.ok(assignments);
    }
}
