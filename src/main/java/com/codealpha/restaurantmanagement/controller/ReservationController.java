package com.codealpha.restaurantmanagement.controller;

import com.codealpha.restaurantmanagement.dto.CreateReservationRequest;
import com.codealpha.restaurantmanagement.dto.ReservationResponse;
import com.codealpha.restaurantmanagement.dto.UpdateReservationStatusRequest;
import com.codealpha.restaurantmanagement.service.ReservationService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody CreateReservationRequest request) {
        ReservationResponse response = reservationService.createReservation(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @GetMapping("/table/{tableId}")
    public ResponseEntity<List<ReservationResponse>> getReservationsByTableId(@PathVariable Long tableId) {
        return ResponseEntity.ok(reservationService.getReservationsByRestaurantTableId(tableId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ReservationResponse> updateReservationStatus(@PathVariable Long id,
                                                                         @Valid @RequestBody UpdateReservationStatusRequest request) {
        return ResponseEntity.ok(reservationService.updateReservationStatus(id, request));
    }
}
