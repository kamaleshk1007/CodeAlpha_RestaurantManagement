package com.codealpha.restaurantmanagement.repository;

import com.codealpha.restaurantmanagement.entity.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByOrderByOrderTimestampDesc();

    List<Order> findByRestaurantTableIdOrderByOrderTimestampDesc(Long restaurantTableId);

    @Query("select o from Order o where o.orderTimestamp >= :start and o.orderTimestamp < :end")
    List<Order> findByOrderTimestampBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.orderTimestamp >= :start and o.orderTimestamp < :end")
    BigDecimal totalAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
