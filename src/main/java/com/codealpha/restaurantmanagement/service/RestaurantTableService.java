package com.codealpha.restaurantmanagement.service;

import com.codealpha.restaurantmanagement.dto.CreateRestaurantTableRequest;
import com.codealpha.restaurantmanagement.dto.RestaurantTableResponse;
import com.codealpha.restaurantmanagement.dto.UpdateRestaurantTableRequest;
import com.codealpha.restaurantmanagement.entity.RestaurantTable;
import com.codealpha.restaurantmanagement.enums.TableStatus;
import com.codealpha.restaurantmanagement.exception.DuplicateResourceException;
import com.codealpha.restaurantmanagement.exception.ResourceNotFoundException;
import com.codealpha.restaurantmanagement.repository.RestaurantTableRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RestaurantTableService {

    private final RestaurantTableRepository restaurantTableRepository;

    public RestaurantTableService(RestaurantTableRepository restaurantTableRepository) {
        this.restaurantTableRepository = restaurantTableRepository;
    }

    @Transactional
    public RestaurantTableResponse createRestaurantTable(CreateRestaurantTableRequest request) {
        ensureTableNumberIsAvailable(request.tableNumber());

        RestaurantTable restaurantTable = new RestaurantTable();
        restaurantTable.setTableNumber(request.tableNumber());
        restaurantTable.setSeatingCapacity(request.seatingCapacity());
        restaurantTable.setStatus(request.status());
        return toResponse(restaurantTableRepository.save(restaurantTable));
    }

    public List<RestaurantTableResponse> getAllRestaurantTables() {
        return restaurantTableRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public RestaurantTableResponse getRestaurantTableById(Long id) {
        return toResponse(findRestaurantTable(id));
    }

    @Transactional
    public RestaurantTableResponse updateRestaurantTable(Long id, UpdateRestaurantTableRequest request) {
        RestaurantTable restaurantTable = findRestaurantTable(id);
        if (!restaurantTable.getTableNumber().equals(request.tableNumber())) {
            ensureTableNumberIsAvailable(request.tableNumber());
        }

        restaurantTable.setTableNumber(request.tableNumber());
        restaurantTable.setSeatingCapacity(request.seatingCapacity());
        restaurantTable.setStatus(request.status());
        return toResponse(restaurantTable);
    }

    @Transactional
    public RestaurantTableResponse updateTableStatus(Long id, TableStatus status) {
        RestaurantTable restaurantTable = findRestaurantTable(id);
        restaurantTable.setStatus(status);
        return toResponse(restaurantTable);
    }

    private void ensureTableNumberIsAvailable(Integer tableNumber) {
        if (restaurantTableRepository.findByTableNumber(tableNumber).isPresent()) {
            throw new DuplicateResourceException("Restaurant table number " + tableNumber + " already exists");
        }
    }

    private RestaurantTable findRestaurantTable(Long id) {
        return restaurantTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant table", id));
    }

    private RestaurantTableResponse toResponse(RestaurantTable restaurantTable) {
        return new RestaurantTableResponse(restaurantTable.getId(), restaurantTable.getTableNumber(),
                restaurantTable.getSeatingCapacity(), restaurantTable.getStatus());
    }
}
