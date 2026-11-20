package com.codealpha.restaurantmanagement.repository;

import com.codealpha.restaurantmanagement.entity.Reservation;
import com.codealpha.restaurantmanagement.enums.ReservationStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = "restaurantTable")
    List<Reservation> findAllByOrderByReservationStartTimeAsc();

    @EntityGraph(attributePaths = "restaurantTable")
    List<Reservation> findByRestaurantTableIdOrderByReservationStartTimeAsc(Long restaurantTableId);

    @Query("""
            select (count(reservation) > 0)
            from Reservation reservation
            where reservation.restaurantTable.id = :tableId
              and reservation.status <> :cancelledStatus
              and reservation.reservationStartTime < :endTime
              and reservation.reservationEndTime > :startTime
            """)
    boolean existsOverlappingReservation(
            @Param("tableId") Long tableId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("cancelledStatus") ReservationStatus cancelledStatus);
}
