package com.restaurant.service;

import com.restaurant.model.RestaurantTable;
import com.restaurant.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RestaurantTableService {

    private final RestaurantTableRepository tableRepository;

    public RestaurantTableService(RestaurantTableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    public List<RestaurantTable> findAll() {
        return tableRepository.findAll();
    }

    public Optional<RestaurantTable> findById(Long id) {
        return tableRepository.findById(id);
    }

    public List<RestaurantTable> findByStatus(RestaurantTable.TableStatus status) {
        return tableRepository.findByStatus(status);
    }

    public RestaurantTable save(RestaurantTable table) {
        return tableRepository.save(table);
    }

    public RestaurantTable updateStatus(Long id, RestaurantTable.TableStatus status) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        table.setStatus(status);
        return tableRepository.save(table);
    }

    public void delete(Long id) {
        tableRepository.deleteById(id);
    }
}