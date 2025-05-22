package com.restaurant.service;

import com.restaurant.model.Reservation;
import com.restaurant.model.RestaurantTable;
import com.restaurant.repository.ReservationRepository;
import com.restaurant.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
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
        return allTables.stream()
                .filter(table -> !reservedTableIds.contains(table.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Guarda una mesa, asignando automáticamente el próximo ID disponible si es una nueva mesa
     */
    @Transactional
    public RestaurantTable save(RestaurantTable table) {
        // Si es una nueva mesa (ID es null), asignar el próximo ID disponible
        if (table.getId() == null) {
            Long nextId = getNextAvailableId();
            table.setId(nextId);
        }
        return tableRepository.save(table);
    }

    /**
     * Encuentra el próximo ID disponible para una nueva mesa
     * @return El próximo ID secuencial disponible
     */
    private Long getNextAvailableId() {
        List<RestaurantTable> allTables = tableRepository.findAll();

        // Si no hay mesas, empezar con ID 1
        if (allTables.isEmpty()) {
            return 1L;
        }

        // Obtener todos los IDs existentes y ordenarlos
        List<Long> existingIds = allTables.stream()
                .map(RestaurantTable::getId)
                .sorted()
                .collect(Collectors.toList());

        // Buscar el primer hueco en la secuencia
        Long expectedId = 1L;
        for (Long existingId : existingIds) {
            if (!existingId.equals(expectedId)) {
                // Encontramos un hueco, devolver el ID esperado
                return expectedId;
            }
            expectedId++;
        }

        // Si no hay huecos, devolver el siguiente ID después del último
        return expectedId;
    }

    /**
     * Actualiza el estado de una mesa
     */
    public RestaurantTable updateStatus(Long id, RestaurantTable.TableStatus status) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        table.setStatus(status);
        return tableRepository.save(table);
    }

    /**
     * Elimina una mesa y reorganiza los IDs para mantener la secuencia
     */
    @Transactional
    public void delete(Long id) {
        // Verificar que la mesa existe
        RestaurantTable tableToDelete = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        // Verificar si la mesa tiene reservas activas
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> activeReservations = reservationRepository
                .findActiveReservationsInTimeRange(now, now.plusDays(365))
                .stream()
                .filter(reservation -> reservation.getTable().getId().equals(id))
                .collect(Collectors.toList());

        if (!activeReservations.isEmpty()) {
            throw new RuntimeException("No se puede eliminar la mesa porque tiene reservas activas");
        }

        // Eliminar la mesa
        tableRepository.deleteById(id);

        // Reorganizar los IDs para mantener la secuencia
        reorganizeTableIds();
    }

    /**
     * Reorganiza los IDs de las mesas para mantener una secuencia sin huecos
     */
    @Transactional
    public void reorganizeTableIds() {
        List<RestaurantTable> allTables = tableRepository.findAll();

        // Ordenar las mesas por ID actual
        allTables.sort(Comparator.comparing(RestaurantTable::getId));

        // Reasignar IDs secuenciales
        Long newId = 1L;
        for (RestaurantTable table : allTables) {
            if (!table.getId().equals(newId)) {
                // Actualizar el ID de la mesa
                table.setId(newId);
                tableRepository.save(table);
            }
            newId++;
        }
    }

    /**
     * Método auxiliar para obtener el conteo total de mesas
     */
    public long getTotalTablesCount() {
        return tableRepository.count();
    }

    /**
     * Verifica si una mesa tiene reservas activas
     */
    public boolean hasActiveReservations(Long tableId) {
        LocalDateTime now = LocalDateTime.now();
        return reservationRepository.findActiveReservationsInTimeRange(now, now.plusDays(365))
                .stream()
                .anyMatch(reservation -> reservation.getTable().getId().equals(tableId));
    }
}