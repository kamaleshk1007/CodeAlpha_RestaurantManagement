package com.codealpha.restaurantmanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codealpha.restaurantmanagement.dto.OrderItemRequest;
import com.codealpha.restaurantmanagement.dto.OrderResponse;
import com.codealpha.restaurantmanagement.dto.PlaceOrderRequest;
import com.codealpha.restaurantmanagement.entity.Inventory;
import com.codealpha.restaurantmanagement.entity.MenuItem;
import com.codealpha.restaurantmanagement.entity.MenuItemIngredient;
import com.codealpha.restaurantmanagement.entity.Order;
import com.codealpha.restaurantmanagement.enums.InventoryUnit;
import com.codealpha.restaurantmanagement.exception.OrderProcessingException;
import com.codealpha.restaurantmanagement.repository.InventoryRepository;
import com.codealpha.restaurantmanagement.repository.MenuItemIngredientRepository;
import com.codealpha.restaurantmanagement.repository.MenuItemRepository;
import com.codealpha.restaurantmanagement.repository.OrderItemRepository;
import com.codealpha.restaurantmanagement.repository.OrderRepository;
import com.codealpha.restaurantmanagement.repository.RestaurantTableRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private MenuItemIngredientRepository menuItemIngredientRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private RestaurantTableRepository restaurantTableRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(menuItemRepository, menuItemIngredientRepository, inventoryRepository,
                restaurantTableRepository, orderRepository, orderItemRepository);
    }

    @Test
    void placeOrderCalculatesTotalsCapturesPricesAndAggregatesSharedIngredients() {
        MenuItem pizza = menuItem(1L, "Pizza", "10.50", true);
        MenuItem pasta = menuItem(2L, "Pasta", "5.25", true);
        Inventory flour = inventory(10L, "Flour", "5.000");
        MenuItemIngredient pizzaRecipe = recipe(pizza, flour, "1.250");
        MenuItemIngredient pastaRecipe = recipe(pasta, flour, "0.500");
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(pizza, pasta));
        when(menuItemIngredientRepository.findByMenuItemIdIn(any()))
                .thenReturn(List.of(pizzaRecipe, pastaRecipe));
        when(inventoryRepository.findByIdsForUpdateOrdered(any())).thenReturn(List.of(flour));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderItemRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.placeOrder(new PlaceOrderRequest(null, List.of(
                new OrderItemRequest(1L, 2), new OrderItemRequest(2L, 3))));

        assertEquals(0, new BigDecimal("36.75").compareTo(response.totalAmount()));
        assertEquals(0, new BigDecimal("21.00").compareTo(response.items().get(0).subtotal()));
        assertEquals(0, new BigDecimal("15.75").compareTo(response.items().get(1).subtotal()));
        assertEquals(0, new BigDecimal("10.50").compareTo(response.items().get(0).unitPrice()));
        assertEquals(0, new BigDecimal("1.000").compareTo(flour.getAvailableQuantity()));
        pizza.setPrice(new BigDecimal("99.99"));
        assertEquals(0, new BigDecimal("10.50").compareTo(response.items().get(0).unitPrice()));

        verify(orderItemRepository).saveAll(any());
    }

    @Test
    void placeOrderRejectsUnavailableMenuItemsBeforeInventoryLookup() {
        MenuItem unavailablePizza = menuItem(1L, "Pizza", "10.50", false);
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(unavailablePizza));

        assertThrows(OrderProcessingException.class, () -> orderService.placeOrder(singleItemOrder(1L, 1)));

        verify(menuItemIngredientRepository, never()).findByMenuItemIdIn(any());
        verify(inventoryRepository, never()).findByIdsForUpdateOrdered(any());
    }

    @Test
    void placeOrderRejectsMenuItemsWithoutRecipes() {
        MenuItem pizza = menuItem(1L, "Pizza", "10.50", true);
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(pizza));
        when(menuItemIngredientRepository.findByMenuItemIdIn(any())).thenReturn(List.of());

        assertThrows(OrderProcessingException.class, () -> orderService.placeOrder(singleItemOrder(1L, 1)));

        verify(inventoryRepository, never()).findByIdsForUpdateOrdered(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrderRejectsInsufficientInventoryWithoutDeductingOrPersistingOrder() {
        MenuItem pizza = menuItem(1L, "Pizza", "10.50", true);
        Inventory flour = inventory(10L, "Flour", "0.500");
        MenuItemIngredient pizzaRecipe = recipe(pizza, flour, "1.000");
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(pizza));
        when(menuItemIngredientRepository.findByMenuItemIdIn(any())).thenReturn(List.of(pizzaRecipe));
        when(inventoryRepository.findByIdsForUpdateOrdered(any())).thenReturn(List.of(flour));

        assertThrows(OrderProcessingException.class, () -> orderService.placeOrder(singleItemOrder(1L, 1)));

        assertEquals(0, new BigDecimal("0.500").compareTo(flour.getAvailableQuantity()));
        verify(orderRepository, never()).save(any());
        verify(orderItemRepository, never()).saveAll(any());
    }

    private PlaceOrderRequest singleItemOrder(Long menuItemId, Integer quantity) {
        return new PlaceOrderRequest(null, List.of(new OrderItemRequest(menuItemId, quantity)));
    }

    private MenuItem menuItem(Long id, String name, String price, boolean available) {
        MenuItem menuItem = new MenuItem();
        ReflectionTestUtils.setField(menuItem, "id", id);
        menuItem.setName(name);
        menuItem.setPrice(new BigDecimal(price));
        menuItem.setAvailable(available);
        return menuItem;
    }

    private Inventory inventory(Long id, String ingredientName, String quantity) {
        Inventory inventory = new Inventory();
        ReflectionTestUtils.setField(inventory, "id", id);
        inventory.setIngredientName(ingredientName);
        inventory.setAvailableQuantity(new BigDecimal(quantity));
        inventory.setUnit(InventoryUnit.KG);
        return inventory;
    }

    private MenuItemIngredient recipe(MenuItem menuItem, Inventory inventory, String requiredQuantity) {
        MenuItemIngredient recipe = org.mockito.Mockito.mock(MenuItemIngredient.class);
        when(recipe.getMenuItem()).thenReturn(menuItem);
        when(recipe.getInventory()).thenReturn(inventory);
        when(recipe.getRequiredQuantity()).thenReturn(new BigDecimal(requiredQuantity));
        return recipe;
    }
}
