package com.codealpha.restaurantmanagement.repository;

import com.codealpha.restaurantmanagement.entity.RestaurantTable;
import com.codealpha.restaurantmanagement.enums.TableStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    List<RestaurantTable> findByStatus(TableStatus status);

    Optional<RestaurantTable> findByTableNumber(Integer tableNumber);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RestaurantTable> findById(Long id);
}
