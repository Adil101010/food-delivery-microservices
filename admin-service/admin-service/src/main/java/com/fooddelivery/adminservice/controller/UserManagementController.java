package com.fooddelivery.adminservice.controller;

import com.fooddelivery.adminservice.dto.ActionRequest;
import com.fooddelivery.adminservice.dto.MessageResponse;
import com.fooddelivery.adminservice.dto.UserListDTO;
import com.fooddelivery.adminservice.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserManagementController {

    private final UserManagementService userManagementService;

    // Get All Users
    @GetMapping
    public ResponseEntity<List<UserListDTO>> getAllUsers() {
        List<UserListDTO> users = userManagementService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Get Users by Role
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserListDTO>> getUsersByRole(@PathVariable String role) {
        List<UserListDTO> users = userManagementService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    // Get User by ID
    @GetMapping("/{id}")
    public ResponseEntity<UserListDTO> getUserById(@PathVariable Long id) {
        UserListDTO user = userManagementService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    // Block User
    @PutMapping("/block")
    public ResponseEntity<MessageResponse> blockUser(@Valid @RequestBody ActionRequest request) {
        userManagementService.blockUser(request.getId());
        return ResponseEntity.ok(new MessageResponse("User blocked successfully"));
    }

    // Unblock User
    @PutMapping("/unblock")
    public ResponseEntity<MessageResponse> unblockUser(@Valid @RequestBody ActionRequest request) {
        userManagementService.unblockUser(request.getId());
        return ResponseEntity.ok(new MessageResponse("User unblocked successfully"));
    }

    // Get All Customers
    @GetMapping("/customers")
    public ResponseEntity<List<UserListDTO>> getAllCustomers() {
        List<UserListDTO> customers = userManagementService.getUsersByRole("CUSTOMER");
        return ResponseEntity.ok(customers);
    }

    // Get All Delivery Partners
    @GetMapping("/delivery-partners")
    public ResponseEntity<List<UserListDTO>> getAllDeliveryPartners() {
        List<UserListDTO> partners = userManagementService.getUsersByRole("DELIVERY_PARTNER");
        return ResponseEntity.ok(partners);
    }

    // Get All Restaurant Owners
    @GetMapping("/restaurant-owners")
    public ResponseEntity<List<UserListDTO>> getAllRestaurantOwners() {
        List<UserListDTO> owners = userManagementService.getUsersByRole("RESTAURANT_OWNER");
        return ResponseEntity.ok(owners);
    }
}
