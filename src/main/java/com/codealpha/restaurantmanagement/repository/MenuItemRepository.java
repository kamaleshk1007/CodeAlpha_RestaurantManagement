package com.codealpha.restaurantmanagement.repository;

import com.codealpha.restaurantmanagement.entity.MenuItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByAvailableTrue();

    boolean existsByNameIgnoreCase(String name);
}
