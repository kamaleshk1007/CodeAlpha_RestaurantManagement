package com.codealpha.restaurantmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "menu_item_ingredients", uniqueConstraints = @UniqueConstraint(
        name = "uk_menu_item_ingredients_menu_inventory", columnNames = {"menu_item_id", "inventory_id"}))
@Check(constraints = "required_quantity > 0")
public class MenuItemIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_menu_item_ingredients_menu_item"))
    private MenuItem menuItem;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_id", nullable = false, foreignKey = @ForeignKey(name = "fk_menu_item_ingredients_inventory"))
    private Inventory inventory;

    @NotNull
    @DecimalMin(value = "0.001", inclusive = true)
    @Column(name = "required_quantity", nullable = false, precision = 19, scale = 3)
    private BigDecimal requiredQuantity;

    protected MenuItemIngredient() {
    }

    public Long getId() { return id; }
    public MenuItem getMenuItem() { return menuItem; }
    public void setMenuItem(MenuItem menuItem) { this.menuItem = menuItem; }
    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }
    public BigDecimal getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(BigDecimal requiredQuantity) { this.requiredQuantity = requiredQuantity; }
}
