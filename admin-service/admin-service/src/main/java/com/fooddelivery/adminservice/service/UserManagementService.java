package com.fooddelivery.adminservice.service;

import com.fooddelivery.adminservice.dto.UserListDTO;
import com.fooddelivery.adminservice.entity.User;
import com.fooddelivery.adminservice.repository.OrderRepository;
import com.fooddelivery.adminservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public List<UserListDTO> getAllUsers() {
        return userRepository.findAllOrderByCreatedAtDesc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UserListDTO> getUsersByRole(String role) {
        return userRepository.findByRole(role).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserListDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToDTO(user);
    }

    public void blockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setIsActive(false);
        userRepository.save(user);
    }

    public void unblockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setIsActive(true);
        userRepository.save(user);
    }

    private UserListDTO convertToDTO(User user) {
        // Count orders for this user (this could be optimized with a join query)
        Long totalOrders = 0L; // Simplified for now

        return UserListDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .totalOrders(totalOrders)
                .build();
    }
}
