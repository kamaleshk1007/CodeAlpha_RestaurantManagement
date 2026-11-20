package com.codealpha.restaurantmanagement.controller;

import com.codealpha.restaurantmanagement.dto.CreateRestaurantTableRequest;
import com.codealpha.restaurantmanagement.dto.RestaurantTableResponse;
import com.codealpha.restaurantmanagement.dto.UpdateRestaurantTableRequest;
import com.codealpha.restaurantmanagement.enums.TableStatus;
import com.codealpha.restaurantmanagement.service.RestaurantTableService;
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
@RequestMapping("/api/tables")
public class RestaurantTableController {

    private final RestaurantTableService restaurantTableService;

    public RestaurantTableController(RestaurantTableService restaurantTableService) {
        this.restaurantTableService = restaurantTableService;
    }

    @PostMapping
    public ResponseEntity<RestaurantTableResponse> createRestaurantTable(
            @Valid @RequestBody CreateRestaurantTableRequest request) {
        RestaurantTableResponse response = restaurantTableService.createRestaurantTable(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RestaurantTableResponse>> getAllRestaurantTables() {
        return ResponseEntity.ok(restaurantTableService.getAllRestaurantTables());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantTableResponse> getRestaurantTableById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantTableService.getRestaurantTableById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantTableResponse> updateRestaurantTable(@PathVariable Long id,
                                                                           @Valid @RequestBody UpdateRestaurantTableRequest request) {
        return ResponseEntity.ok(restaurantTableService.updateRestaurantTable(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RestaurantTableResponse> updateTableStatus(@PathVariable Long id,
                                                                       @RequestParam TableStatus status) {
        return ResponseEntity.ok(restaurantTableService.updateTableStatus(id, status));
    }
}
