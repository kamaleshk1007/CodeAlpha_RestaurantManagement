package com.codealpha.restaurantmanagement.controller;

import com.codealpha.restaurantmanagement.dto.CreateInventoryRequest;
import com.codealpha.restaurantmanagement.dto.InventoryResponse;
import com.codealpha.restaurantmanagement.dto.UpdateInventoryRequest;
import com.codealpha.restaurantmanagement.service.InventoryService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> createInventoryItem(@Valid @RequestBody CreateInventoryRequest request) {
        InventoryResponse response = inventoryService.createInventoryItem(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllInventoryItems() {
        return ResponseEntity.ok(inventoryService.getAllInventoryItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponse> getInventoryItemById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventoryItemById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryResponse> updateInventoryItem(@PathVariable Long id,
                                                                   @Valid @RequestBody UpdateInventoryRequest request) {
        return ResponseEntity.ok(inventoryService.updateInventoryItem(id, request));
    }
}
