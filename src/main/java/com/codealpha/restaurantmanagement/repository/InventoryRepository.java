package com.codealpha.restaurantmanagement.repository;

import com.codealpha.restaurantmanagement.entity.Inventory;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByIngredientNameIgnoreCase(String ingredientName);

    boolean existsByIngredientNameIgnoreCase(String ingredientName);

    List<Inventory> findByAvailableQuantityLessThan(BigDecimal threshold);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Inventory> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findByIdIn(Collection<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select inventory from Inventory inventory where inventory.id in :ids order by inventory.id")
    List<Inventory> findByIdsForUpdateOrdered(@Param("ids") Collection<Long> ids);
}
