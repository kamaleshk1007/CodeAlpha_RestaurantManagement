package com.codealpha.restaurantmanagement.repository;

import com.codealpha.restaurantmanagement.entity.MenuItemIngredient;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemIngredientRepository extends JpaRepository<MenuItemIngredient, Long> {

    @EntityGraph(attributePaths = "inventory")
    List<MenuItemIngredient> findByMenuItemId(Long menuItemId);

    @EntityGraph(attributePaths = "inventory")
    List<MenuItemIngredient> findByMenuItemIdIn(Collection<Long> menuItemIds);
}
