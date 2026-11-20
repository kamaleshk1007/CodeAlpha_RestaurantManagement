package com.codealpha.restaurantmanagement.service;

import com.codealpha.restaurantmanagement.dto.OrderItemResponse;
import com.codealpha.restaurantmanagement.dto.OrderResponse;
import com.codealpha.restaurantmanagement.dto.PlaceOrderRequest;
import com.codealpha.restaurantmanagement.entity.Inventory;
import com.codealpha.restaurantmanagement.entity.MenuItem;
import com.codealpha.restaurantmanagement.entity.MenuItemIngredient;
import com.codealpha.restaurantmanagement.entity.Order;
import com.codealpha.restaurantmanagement.entity.OrderItem;
import com.codealpha.restaurantmanagement.entity.RestaurantTable;
import com.codealpha.restaurantmanagement.enums.OrderStatus;
import com.codealpha.restaurantmanagement.exception.OrderProcessingException;
import com.codealpha.restaurantmanagement.exception.ResourceNotFoundException;
import com.codealpha.restaurantmanagement.repository.InventoryRepository;
import com.codealpha.restaurantmanagement.repository.MenuItemIngredientRepository;
import com.codealpha.restaurantmanagement.repository.MenuItemRepository;
import com.codealpha.restaurantmanagement.repository.OrderItemRepository;
import com.codealpha.restaurantmanagement.repository.OrderRepository;
import com.codealpha.restaurantmanagement.repository.RestaurantTableRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final MenuItemRepository menuItemRepository;
    private final MenuItemIngredientRepository menuItemIngredientRepository;
    private final InventoryRepository inventoryRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(MenuItemRepository menuItemRepository,
                        MenuItemIngredientRepository menuItemIngredientRepository,
                        InventoryRepository inventoryRepository,
                        RestaurantTableRepository restaurantTableRepository,
                        OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository) {
        this.menuItemRepository = menuItemRepository;
        this.menuItemIngredientRepository = menuItemIngredientRepository;
        this.inventoryRepository = inventoryRepository;
        this.restaurantTableRepository = restaurantTableRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        Map<Long, Integer> quantitiesByMenuItemId = aggregateOrderedQuantities(request);
        Map<Long, MenuItem> menuItemsById = loadAvailableMenuItems(quantitiesByMenuItemId.keySet());
        RestaurantTable restaurantTable = findOptionalRestaurantTable(request.restaurantTableId());
        List<MenuItemIngredient> recipes = menuItemIngredientRepository
                .findByMenuItemIdIn(quantitiesByMenuItemId.keySet());
        Map<Long, List<MenuItemIngredient>> recipesByMenuItemId = groupRecipesByMenuItem(recipes);
        ensureEveryMenuItemHasRecipe(quantitiesByMenuItemId.keySet(), recipesByMenuItemId);

        Map<Long, BigDecimal> requiredQuantityByInventoryId = calculateRequiredInventory(
                quantitiesByMenuItemId, recipesByMenuItemId);
        List<Inventory> lockedInventory = lockInventoryInConsistentOrder(requiredQuantityByInventoryId.keySet());
        verifyAndDeductInventory(requiredQuantityByInventoryId, lockedInventory);

        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setOrderTimestamp(LocalDateTime.now());
        order.setRestaurantTable(restaurantTable);

        List<OrderItem> orderItems = createOrderItems(order, quantitiesByMenuItemId, menuItemsById);
        order.setTotalAmount(calculateTotal(orderItems));
        Order savedOrder = orderRepository.save(order);
        List<OrderItem> savedOrderItems = orderItemRepository.saveAll(orderItems);
        return toResponse(savedOrder, savedOrderItems);
    }

    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByOrderTimestampDesc();
        if (orders.isEmpty()) {
            return List.of();
        }
        Map<Long, List<OrderItem>> orderItemsByOrderId = orderItemRepository.findByOrderIdIn(
                        orders.stream().map(Order::getId).toList())
                .stream().collect(java.util.stream.Collectors.groupingBy(orderItem -> orderItem.getOrder().getId()));
        return orders.stream()
                .map(order -> toResponse(order, orderItemsByOrderId.getOrDefault(order.getId(), List.of())))
                .toList();
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        return toResponse(order, orderItemRepository.findByOrderId(id));
    }

    private Map<Long, Integer> aggregateOrderedQuantities(PlaceOrderRequest request) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new OrderProcessingException("An order must contain at least one item");
        }

        Map<Long, Integer> quantitiesByMenuItemId = new LinkedHashMap<>();
        request.items().forEach(item -> {
            if (item == null || item.menuItemId() == null || item.menuItemId() <= 0
                    || item.quantity() == null || item.quantity() <= 0) {
                throw new OrderProcessingException("Order item identifiers and quantities must be positive");
            }
            quantitiesByMenuItemId.merge(item.menuItemId(), item.quantity(), Math::addExact);
        });
        return quantitiesByMenuItemId;
    }

    private Map<Long, MenuItem> loadAvailableMenuItems(Collection<Long> menuItemIds) {
        Map<Long, MenuItem> menuItemsById = new LinkedHashMap<>();
        menuItemRepository.findAllById(menuItemIds).forEach(menuItem -> {
            if (Boolean.TRUE.equals(menuItem.getAvailable())) {
                menuItemsById.put(menuItem.getId(), menuItem);
            }
        });

        if (menuItemsById.size() != menuItemIds.size()) {
            throw new OrderProcessingException("One or more menu items are missing or unavailable");
        }
        return menuItemsById;
    }

    private RestaurantTable findOptionalRestaurantTable(Long restaurantTableId) {
        if (restaurantTableId == null) {
            return null;
        }
        return restaurantTableRepository.findById(restaurantTableId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant table", restaurantTableId));
    }

    private Map<Long, List<MenuItemIngredient>> groupRecipesByMenuItem(List<MenuItemIngredient> recipes) {
        Map<Long, List<MenuItemIngredient>> recipesByMenuItemId = new LinkedHashMap<>();
        for (MenuItemIngredient recipe : recipes) {
            recipesByMenuItemId.computeIfAbsent(recipe.getMenuItem().getId(), ignored -> new ArrayList<>()).add(recipe);
        }
        return recipesByMenuItemId;
    }

    private void ensureEveryMenuItemHasRecipe(Collection<Long> menuItemIds,
                                               Map<Long, List<MenuItemIngredient>> recipesByMenuItemId) {
        if (menuItemIds.stream().anyMatch(menuItemId -> !recipesByMenuItemId.containsKey(menuItemId))) {
            throw new OrderProcessingException("Every ordered menu item must have a recipe");
        }
    }

    private Map<Long, BigDecimal> calculateRequiredInventory(
            Map<Long, Integer> quantitiesByMenuItemId,
            Map<Long, List<MenuItemIngredient>> recipesByMenuItemId) {
        Map<Long, BigDecimal> requiredQuantityByInventoryId = new LinkedHashMap<>();
        quantitiesByMenuItemId.forEach((menuItemId, quantity) -> recipesByMenuItemId.get(menuItemId).forEach(recipe -> {
            if (recipe.getInventory() == null || recipe.getInventory().getId() == null
                    || recipe.getRequiredQuantity() == null || recipe.getRequiredQuantity().signum() <= 0) {
                throw new OrderProcessingException("Menu item recipe is invalid");
            }
            BigDecimal requiredQuantity = recipe.getRequiredQuantity().multiply(BigDecimal.valueOf(quantity));
            requiredQuantityByInventoryId.merge(recipe.getInventory().getId(), requiredQuantity, BigDecimal::add);
        }));
        return requiredQuantityByInventoryId;
    }

    private List<Inventory> lockInventoryInConsistentOrder(Collection<Long> inventoryIds) {
        List<Long> sortedInventoryIds = inventoryIds.stream().sorted(Comparator.naturalOrder()).toList();
        List<Inventory> lockedInventory = inventoryRepository.findByIdsForUpdateOrdered(sortedInventoryIds);
        if (lockedInventory.size() != sortedInventoryIds.size()) {
            throw new OrderProcessingException("One or more inventory items are unavailable");
        }
        return lockedInventory;
    }

    private void verifyAndDeductInventory(Map<Long, BigDecimal> requiredQuantityByInventoryId,
                                           List<Inventory> lockedInventory) {
        Map<Long, Inventory> inventoryById = lockedInventory.stream()
                .collect(java.util.stream.Collectors.toMap(Inventory::getId, inventory -> inventory));

        for (Map.Entry<Long, BigDecimal> requirement : requiredQuantityByInventoryId.entrySet()) {
            Inventory inventory = inventoryById.get(requirement.getKey());
            if (inventory == null || inventory.getAvailableQuantity() == null
                    || inventory.getAvailableQuantity().compareTo(requirement.getValue()) < 0) {
                throw new OrderProcessingException("Insufficient inventory for an ordered menu item");
            }
        }
        requiredQuantityByInventoryId.forEach((inventoryId, requiredQuantity) -> {
            Inventory inventory = Objects.requireNonNull(inventoryById.get(inventoryId));
            inventory.setAvailableQuantity(inventory.getAvailableQuantity().subtract(requiredQuantity));
        });
    }

    private List<OrderItem> createOrderItems(Order order,
                                             Map<Long, Integer> quantitiesByMenuItemId,
                                             Map<Long, MenuItem> menuItemsById) {
        return quantitiesByMenuItemId.entrySet().stream().map(entry -> {
            MenuItem menuItem = menuItemsById.get(entry.getKey());
            BigDecimal unitPrice = menuItem.getPrice();
            if (unitPrice == null || unitPrice.signum() < 0) {
                throw new OrderProcessingException("Menu item price is invalid");
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(entry.getValue());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(entry.getValue())));
            return orderItem;
        }).toList();
    }

    private BigDecimal calculateTotal(List<OrderItem> orderItems) {
        return orderItems.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrderResponse toResponse(Order order, List<OrderItem> orderItems) {
        List<OrderItemResponse> itemResponses = orderItems.stream().map(orderItem -> new OrderItemResponse(
                orderItem.getId(), orderItem.getMenuItem().getId(), orderItem.getMenuItem().getName(),
                orderItem.getQuantity(), orderItem.getUnitPrice(), orderItem.getSubtotal())).toList();
        Long restaurantTableId = order.getRestaurantTable() == null ? null : order.getRestaurantTable().getId();
        return new OrderResponse(order.getId(), order.getStatus(), order.getOrderTimestamp(), order.getTotalAmount(),
                restaurantTableId, itemResponses);
    }
}
