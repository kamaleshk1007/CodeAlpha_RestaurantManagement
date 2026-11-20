package com.codealpha.restaurantmanagement.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

import com.codealpha.restaurantmanagement.dto.UpdateInventoryRequest;
import com.codealpha.restaurantmanagement.repository.InventoryRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class InventoryServiceTest {

    @Test
    void updateInventoryItemRejectsNegativeStock() {
        InventoryRepository inventoryRepository = Mockito.mock(InventoryRepository.class);
        InventoryService service = new InventoryService(inventoryRepository);
        UpdateInventoryRequest request = new UpdateInventoryRequest("Flour", new BigDecimal("-0.001"));

        assertThrows(IllegalArgumentException.class, () -> service.updateInventoryItem(1L, request));

        verifyNoInteractions(inventoryRepository);
    }
}
