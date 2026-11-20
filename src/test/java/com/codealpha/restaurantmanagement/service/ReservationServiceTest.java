package com.codealpha.restaurantmanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codealpha.restaurantmanagement.dto.CreateReservationRequest;
import com.codealpha.restaurantmanagement.dto.ReservationResponse;
import com.codealpha.restaurantmanagement.entity.Reservation;
import com.codealpha.restaurantmanagement.entity.RestaurantTable;
import com.codealpha.restaurantmanagement.enums.ReservationStatus;
import com.codealpha.restaurantmanagement.enums.TableStatus;
import com.codealpha.restaurantmanagement.exception.ReservationConflictException;
import com.codealpha.restaurantmanagement.repository.ReservationRepository;
import com.codealpha.restaurantmanagement.repository.RestaurantTableRepository;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RestaurantTableRepository restaurantTableRepository;

    @Test
    void createReservationRejectsAnOverlappingActiveReservation() {
        RestaurantTable restaurantTable = restaurantTable(4L, 4);
        when(restaurantTableRepository.findById(4L)).thenReturn(Optional.of(restaurantTable));
        when(reservationRepository.existsOverlappingReservation(any(), any(), any(), any())).thenReturn(true);

        ReservationService service = new ReservationService(reservationRepository, restaurantTableRepository);

        assertThrows(ReservationConflictException.class, () -> service.createReservation(request()));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservationUsesLockedTableLookupAndCreatesPendingReservation() {
        RestaurantTable restaurantTable = restaurantTable(4L, 4);
        when(restaurantTableRepository.findById(4L)).thenReturn(Optional.of(restaurantTable));
        when(reservationRepository.existsOverlappingReservation(any(), any(), any(), any())).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ReservationService service = new ReservationService(reservationRepository, restaurantTableRepository);

        ReservationResponse response = service.createReservation(request());

        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(reservationCaptor.capture());
        assertEquals(ReservationStatus.PENDING, reservationCaptor.getValue().getStatus());
        assertEquals(4L, response.restaurantTableId());
        verify(restaurantTableRepository).findById(4L);
    }

    @Test
    void restaurantTableLookupUsesPessimisticWriteLock() throws NoSuchMethodException {
        Lock lock = RestaurantTableRepository.class.getMethod("findById", Long.class).getAnnotation(Lock.class);

        assertNotNull(lock);
        assertEquals(LockModeType.PESSIMISTIC_WRITE, lock.value());
    }

    private CreateReservationRequest request() {
        LocalDateTime startTime = LocalDateTime.of(2026, 11, 6, 18, 0);
        return new CreateReservationRequest("Asha", "asha@example.com", 4, startTime, startTime.plusHours(2), 4L);
    }

    private RestaurantTable restaurantTable(Long id, int seatingCapacity) {
        RestaurantTable restaurantTable = new RestaurantTable();
        ReflectionTestUtils.setField(restaurantTable, "id", id);
        restaurantTable.setSeatingCapacity(seatingCapacity);
        restaurantTable.setStatus(TableStatus.AVAILABLE);
        return restaurantTable;
    }
}
