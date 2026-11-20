package com.codealpha.restaurantmanagement.entity;

import com.codealpha.restaurantmanagement.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "orders")
@Check(constraints = "total_amount >= 0")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @NotNull
    @Column(name = "order_timestamp", nullable = false)
    private LocalDateTime orderTimestamp;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = true)
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_table_id", foreignKey = @ForeignKey(name = "fk_orders_restaurant_table"))
    private RestaurantTable restaurantTable;

    public Order() {
    }

    public Long getId() { return id; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public LocalDateTime getOrderTimestamp() { return orderTimestamp; }
    public void setOrderTimestamp(LocalDateTime orderTimestamp) { this.orderTimestamp = orderTimestamp; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public RestaurantTable getRestaurantTable() { return restaurantTable; }
    public void setRestaurantTable(RestaurantTable restaurantTable) { this.restaurantTable = restaurantTable; }
}
