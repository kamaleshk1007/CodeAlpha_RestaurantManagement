package com.codealpha.restaurantmanagement.entity;

import com.codealpha.restaurantmanagement.enums.InventoryUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "inventory")
@Check(constraints = "available_quantity >= 0")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "ingredient_name", nullable = false, unique = true, length = 120)
    private String ingredientName;

    @NotNull
    @DecimalMin(value = "0.000", inclusive = true)
    @Column(name = "available_quantity", nullable = false, precision = 19, scale = 3)
    private BigDecimal availableQuantity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private InventoryUnit unit;

    public Inventory() {
    }

    public Long getId() { return id; }
    public String getIngredientName() { return ingredientName; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }
    public BigDecimal getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(BigDecimal availableQuantity) { this.availableQuantity = availableQuantity; }
    public InventoryUnit getUnit() { return unit; }
    public void setUnit(InventoryUnit unit) { this.unit = unit; }
}
