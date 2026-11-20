package com.codealpha.restaurantmanagement.entity;

import com.codealpha.restaurantmanagement.enums.ReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "reservations", indexes = @Index(
        name = "idx_reservations_table_start_end",
        columnList = "restaurant_table_id,reservation_start_time,reservation_end_time"))
@Check(constraints = "reservation_end_time > reservation_start_time")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;

    @NotBlank
    @Column(name = "customer_contact", nullable = false, length = 120)
    private String customerContact;

    @NotNull
    @Min(1)
    @Column(name = "party_size", nullable = false)
    private Integer partySize;

    @NotNull
    @Column(name = "reservation_start_time", nullable = false)
    private LocalDateTime reservationStartTime;

    @NotNull
    @Column(name = "reservation_end_time", nullable = false)
    private LocalDateTime reservationEndTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_table_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reservations_restaurant_table"))
    private RestaurantTable restaurantTable;

    public Reservation() {
    }

    @AssertTrue(message = "reservation end time must be after reservation start time")
    public boolean isReservationTimeRangeValid() {
        return reservationStartTime == null || reservationEndTime == null
                || reservationEndTime.isAfter(reservationStartTime);
    }

    public Long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerContact() { return customerContact; }
    public void setCustomerContact(String customerContact) { this.customerContact = customerContact; }
    public Integer getPartySize() { return partySize; }
    public void setPartySize(Integer partySize) { this.partySize = partySize; }
    public LocalDateTime getReservationStartTime() { return reservationStartTime; }
    public void setReservationStartTime(LocalDateTime reservationStartTime) { this.reservationStartTime = reservationStartTime; }
    public LocalDateTime getReservationEndTime() { return reservationEndTime; }
    public void setReservationEndTime(LocalDateTime reservationEndTime) { this.reservationEndTime = reservationEndTime; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public RestaurantTable getRestaurantTable() { return restaurantTable; }
    public void setRestaurantTable(RestaurantTable restaurantTable) { this.restaurantTable = restaurantTable; }
}
