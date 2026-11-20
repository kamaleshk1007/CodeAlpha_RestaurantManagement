package com.codealpha.restaurantmanagement.service;

import com.codealpha.restaurantmanagement.dto.CreateMenuItemRequest;
import com.codealpha.restaurantmanagement.dto.MenuItemResponse;
import com.codealpha.restaurantmanagement.dto.UpdateMenuItemRequest;
import com.codealpha.restaurantmanagement.entity.MenuItem;
import com.codealpha.restaurantmanagement.exception.DuplicateResourceException;
import com.codealpha.restaurantmanagement.exception.ResourceNotFoundException;
import com.codealpha.restaurantmanagement.repository.MenuItemRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MenuService {

    private final MenuItemRepository menuItemRepository;

    public MenuService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional
    public MenuItemResponse createMenuItem(CreateMenuItemRequest request) {
        String name = request.name().trim();
        if (menuItemRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("A menu item named '" + name + "' already exists");
        }

        MenuItem menuItem = new MenuItem();
        menuItem.setName(name);
        menuItem.setDescription(request.description());
        menuItem.setPrice(request.price());
        menuItem.setAvailable(request.available());
        return toResponse(menuItemRepository.save(menuItem));
    }

    public List<MenuItemResponse> getAllMenuItems() {
        return menuItemRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<MenuItemResponse> getAvailableMenuItems() {
        return menuItemRepository.findByAvailableTrue().stream().map(this::toResponse).toList();
    }

    public MenuItemResponse getMenuItemById(Long id) {
        return toResponse(findMenuItem(id));
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Long id, UpdateMenuItemRequest request) {
        MenuItem menuItem = findMenuItem(id);
        String name = request.name().trim();
        if (!menuItem.getName().equalsIgnoreCase(name) && menuItemRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("A menu item named '" + name + "' already exists");
        }

        menuItem.setName(name);
        menuItem.setDescription(request.description());
        menuItem.setPrice(request.price());
        menuItem.setAvailable(request.available());
        return toResponse(menuItem);
    }

    @Transactional
    public MenuItemResponse setMenuItemAvailability(Long id, boolean available) {
        MenuItem menuItem = findMenuItem(id);
        menuItem.setAvailable(available);
        return toResponse(menuItem);
    }

    private MenuItem findMenuItem(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item", id));
    }

    private MenuItemResponse toResponse(MenuItem menuItem) {
        return new MenuItemResponse(menuItem.getId(), menuItem.getName(), menuItem.getDescription(),
                menuItem.getPrice(), menuItem.getAvailable());
    }
}
