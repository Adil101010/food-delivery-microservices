// user-service/src/main/java/com/fooddelivery/userservice/repository/AddressRepository.java

package com.fooddelivery.userservice.repository;

import com.fooddelivery.userservice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);

    Optional<Address> findByIdAndUserId(Long id, Long userId);

    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.userId = :userId")
    void resetDefaultAddress(Long userId);

    long countByUserId(Long userId);

    void deleteByIdAndUserId(Long id, Long userId);
}
