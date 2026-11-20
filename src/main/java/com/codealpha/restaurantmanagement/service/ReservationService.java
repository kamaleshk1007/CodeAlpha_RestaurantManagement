package com.codealpha.restaurantmanagement.service;

import com.codealpha.restaurantmanagement.dto.CreateReservationRequest;
import com.codealpha.restaurantmanagement.dto.ReservationResponse;
import com.codealpha.restaurantmanagement.dto.UpdateReservationStatusRequest;
import com.codealpha.restaurantmanagement.entity.Reservation;
import com.codealpha.restaurantmanagement.entity.RestaurantTable;
import com.codealpha.restaurantmanagement.enums.ReservationStatus;
import com.codealpha.restaurantmanagement.enums.TableStatus;
import com.codealpha.restaurantmanagement.exception.ReservationConflictException;
import com.codealpha.restaurantmanagement.exception.ResourceNotFoundException;
import com.codealpha.restaurantmanagement.repository.ReservationRepository;
import com.codealpha.restaurantmanagement.repository.RestaurantTableRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantTableRepository restaurantTableRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              RestaurantTableRepository restaurantTableRepository) {
        this.reservationRepository = reservationRepository;
        this.restaurantTableRepository = restaurantTableRepository;
    }

    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request) {
        validateTimeRange(request.reservationStartTime(), request.reservationEndTime());
        RestaurantTable restaurantTable = restaurantTableRepository.findById(request.restaurantTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant table", request.restaurantTableId()));

        if (restaurantTable.getStatus() == TableStatus.OUT_OF_SERVICE) {
            throw new ReservationConflictException("Restaurant table is out of service");
        }
        if (request.partySize() > restaurantTable.getSeatingCapacity()) {
            throw new ReservationConflictException("Restaurant table does not have enough seating capacity");
        }
        if (reservationRepository.existsOverlappingReservation(restaurantTable.getId(),
                request.reservationStartTime(), request.reservationEndTime(), ReservationStatus.CANCELLED)) {
            throw new ReservationConflictException("Restaurant table already has an overlapping reservation");
        }

        Reservation reservation = new Reservation();
        reservation.setCustomerName(request.customerName().trim());
        reservation.setCustomerContact(request.customerContact().trim());
        reservation.setPartySize(request.partySize());
        reservation.setReservationStartTime(request.reservationStartTime());
        reservation.setReservationEndTime(request.reservationEndTime());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setRestaurantTable(restaurantTable);
        return toResponse(reservationRepository.save(reservation));
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAllByOrderByReservationStartTimeAsc().stream().map(this::toResponse).toList();
    }

    public ReservationResponse getReservationById(Long id) {
        return toResponse(findReservation(id));
    }

    public List<ReservationResponse> getReservationsByRestaurantTableId(Long restaurantTableId) {
        return reservationRepository.findByRestaurantTableIdOrderByReservationStartTimeAsc(restaurantTableId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public ReservationResponse updateReservationStatus(Long id, UpdateReservationStatusRequest request) {
        Reservation reservation = findReservation(id);
        reservation.setStatus(request.status());
        return toResponse(reservation);
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new ReservationConflictException("Reservation end time must be after reservation start time");
        }
    }

    private Reservation findReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
    }

    private ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(reservation.getId(), reservation.getCustomerName(), reservation.getCustomerContact(),
                reservation.getPartySize(), reservation.getReservationStartTime(), reservation.getReservationEndTime(),
                reservation.getStatus(), reservation.getRestaurantTable().getId());
    }
}
