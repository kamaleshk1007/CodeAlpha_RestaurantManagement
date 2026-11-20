package com.codealpha.restaurantmanagement.repository;

import com.codealpha.restaurantmanagement.entity.OrderItem;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @EntityGraph(attributePaths = "menuItem")
    List<OrderItem> findByOrderId(Long orderId);

    @EntityGraph(attributePaths = {"menuItem", "order"})
    List<OrderItem> findByOrderIdIn(Collection<Long> orderIds);
}
