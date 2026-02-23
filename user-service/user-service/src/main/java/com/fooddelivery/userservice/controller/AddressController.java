// user-service/src/main/java/com/fooddelivery/userservice/controller/AddressController.java

package com.fooddelivery.userservice.controller;

import com.fooddelivery.userservice.dto.AddressRequest;
import com.fooddelivery.userservice.dto.AddressResponse;
import com.fooddelivery.userservice.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AddressResponse>> getUserAddresses(@PathVariable Long userId) {
        System.out.println("GET /api/addresses/user/" + userId);
        List<AddressResponse> addresses = addressService.getUserAddresses(userId);
        return ResponseEntity.ok(addresses);
    }

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddressRequest request) {
        System.out.println("POST /api/addresses for user: " + userId);
        AddressResponse address = addressService.addAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> updateAddress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        System.out.println("PUT /api/addresses/" + id + " for user: " + userId);
        AddressResponse address = addressService.updateAddress(userId, id, request);
        return ResponseEntity.ok(address);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAddress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        System.out.println("DELETE /api/addresses/" + id + " for user: " + userId);
        addressService.deleteAddress(userId, id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Address deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<AddressResponse> setDefaultAddress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        System.out.println("PATCH /api/addresses/" + id + "/default for user: " + userId);
        AddressResponse address = addressService.setDefaultAddress(userId, id);
        return ResponseEntity.ok(address);
    }

    @GetMapping("/default")
    public ResponseEntity<AddressResponse> getDefaultAddress(
            @RequestHeader("X-User-Id") Long userId) {
        System.out.println("GET /api/addresses/default for user: " + userId);
        AddressResponse address = addressService.getDefaultAddress(userId);
        return ResponseEntity.ok(address);
    }
}
