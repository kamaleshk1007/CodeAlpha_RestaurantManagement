package com.codealpha.restaurantmanagement.controller;

import com.codealpha.restaurantmanagement.dto.CreateMenuItemRequest;
import com.codealpha.restaurantmanagement.dto.MenuItemResponse;
import com.codealpha.restaurantmanagement.dto.UpdateMenuItemRequest;
import com.codealpha.restaurantmanagement.service.MenuService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping
    public ResponseEntity<MenuItemResponse> createMenuItem(@Valid @RequestBody CreateMenuItemRequest request) {
        MenuItemResponse response = menuService.createMenuItem(request);
        return ResponseEntity.created(locationFor(response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MenuItemResponse>> getAllMenuItems() {
        return ResponseEntity.ok(menuService.getAllMenuItems());
    }

    @GetMapping("/available")
    public ResponseEntity<List<MenuItemResponse>> getAvailableMenuItems() {
        return ResponseEntity.ok(menuService.getAvailableMenuItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponse> getMenuItemById(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.getMenuItemById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateMenuItemRequest request) {
        return ResponseEntity.ok(menuService.updateMenuItem(id, request));
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<MenuItemResponse> updateAvailability(@PathVariable Long id,
                                                                @RequestParam boolean available) {
        return ResponseEntity.ok(menuService.setMenuItemAvailability(id, available));
    }

    private URI locationFor(Long id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
