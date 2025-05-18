package com.restaurant.service;

import com.restaurant.model.Reservation;
import com.restaurant.model.RestaurantTable;
import com.restaurant.repository.ReservationRepository;
import com.restaurant.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RestaurantTableService {

    private final RestaurantTableRepository tableRepository;
    private final ReservationRepository reservationRepository;

    public RestaurantTableService(RestaurantTableRepository tableRepository, ReservationRepository reservationRepository) {
        this.tableRepository = tableRepository;
        this.reservationRepository = reservationRepository;
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

    /**
     * Encuentra mesas disponibles para una fecha y hora específicas
     * @param dateTime La fecha y hora para la reserva
     * @return Lista de mesas disponibles
     */
    public List<RestaurantTable> findAvailableTablesForDateTime(LocalDateTime dateTime) {
        // Si la fecha es nula, devolvemos todas las mesas disponibles
        if (dateTime == null) {
            return findByStatus(RestaurantTable.TableStatus.AVAILABLE);
        }

        // Definir ventana de tiempo para todo el día (desde las 00:00 hasta las 23:59)
        LocalDateTime startOfDay = dateTime.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = dateTime.toLocalDate().atTime(23, 59, 59);

        // Obtener todas las mesas - CAMBIO: obtener todas, no solo las disponibles
        List<RestaurantTable> allTables = tableRepository.findAll();

        // Obtener IDs de mesas que ya tienen reservas en ese día
        List<Long> reservedTableIds = reservationRepository.findActiveReservationsInTimeRange(startOfDay, endOfDay)
                .stream()
                .map(reservation -> reservation.getTable().getId())
                .collect(Collectors.toList());

        // Filtrar mesas que no estén reservadas en ese día
        // CAMBIO: No consideramos el estado de la mesa, solo si tiene reservas activas
        return allTables.stream()
                .filter(table -> !reservedTableIds.contains(table.getId()))
                .collect(Collectors.toList());
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