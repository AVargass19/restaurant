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

    /**
     * Encuentra mesas por estado - NOTA: Este método ahora es menos útil
     * ya que el estado se calcula dinámicamente según las reservas
     */
    public List<RestaurantTable> findByStatus(RestaurantTable.TableStatus status) {
        if (status == RestaurantTable.TableStatus.AVAILABLE) {
            // Para "disponible", calculamos dinámicamente para hoy
            return findAvailableTablesForDateTime(LocalDateTime.now());
        } else {
            // Para otros estados, usar el método tradicional
            return tableRepository.findByStatus(status);
        }
    }

    /**
     * Encuentra mesas disponibles para una fecha y hora específicas
     * @param dateTime La fecha y hora para la reserva
     * @return Lista de mesas disponibles
     */
    public List<RestaurantTable> findAvailableTablesForDateTime(LocalDateTime dateTime) {
        // Si la fecha es nula, usar fecha actual
        if (dateTime == null) {
            dateTime = LocalDateTime.now();
        }

        // Definir ventana de tiempo para todo el día (desde las 00:00 hasta las 23:59)
        LocalDateTime startOfDay = dateTime.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = dateTime.toLocalDate().atTime(23, 59, 59);

        // Obtener todas las mesas
        List<RestaurantTable> allTables = tableRepository.findAll();

        // Obtener IDs de mesas que ya tienen reservas ACTIVAS en ese día específico
        List<Long> reservedTableIds = reservationRepository.findActiveReservationsInTimeRange(startOfDay, endOfDay)
                .stream()
                .map(reservation -> reservation.getTable().getId())
                .collect(Collectors.toList());

        // Filtrar mesas que no estén reservadas en ese día específico
        return allTables.stream()
                .filter(table -> !reservedTableIds.contains(table.getId()))
                .sorted(Comparator.comparing(RestaurantTable::getId)) // Ordenar por ID
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el estado dinámico de una mesa para una fecha específica
     * @param tableId ID de la mesa
     * @param date Fecha para verificar (si es null, usa fecha actual)
     * @return Estado de la mesa para esa fecha
     */
    public RestaurantTable.TableStatus getTableStatusForDate(Long tableId, LocalDateTime date) {
        if (date == null) {
            date = LocalDateTime.now();
        }

        // Definir ventana de tiempo para todo el día
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().atTime(23, 59, 59);

        // Verificar si hay reservas activas para esta mesa en esta fecha
        List<Reservation> activeReservations = reservationRepository.findActiveReservationsInTimeRange(startOfDay, endOfDay)
                .stream()
                .filter(reservation -> reservation.getTable().getId().equals(tableId))
                .collect(Collectors.toList());

        if (!activeReservations.isEmpty()) {
            return RestaurantTable.TableStatus.RESERVED;
        }

        // Si no hay reservas activas, verificar el estado base de la mesa
        RestaurantTable table = tableRepository.findById(tableId).orElse(null);
        if (table != null && table.getStatus() == RestaurantTable.TableStatus.OCCUPIED) {
            // Solo mantener OCCUPIED si es para el día actual
            if (date.toLocalDate().equals(LocalDateTime.now().toLocalDate())) {
                return RestaurantTable.TableStatus.OCCUPIED;
            }
        }

        return RestaurantTable.TableStatus.AVAILABLE;
    }

    /**
     * Obtiene una lista de mesas con su estado calculado dinámicamente para una fecha
     * @param date Fecha para calcular estados (si es null, usa fecha actual)
     * @return Lista de mesas con estados actualizados
     */
    public List<RestaurantTable> getTablesWithDynamicStatus(LocalDateTime date) {
        if (date == null) {
            date = LocalDateTime.now();
        }

        List<RestaurantTable> allTables = findAll();

        // Calcular el estado dinámico para cada mesa
        for (RestaurantTable table : allTables) {
            RestaurantTable.TableStatus dynamicStatus = getTableStatusForDate(table.getId(), date);
            table.setStatus(dynamicStatus); // Establecer el estado calculado
        }

        return allTables.stream()
                .sorted(Comparator.comparing(RestaurantTable::getId))
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
     * Actualiza el estado base de una mesa (solo para OCCUPIED/AVAILABLE manual)
     * NOTA: Los estados RESERVED se manejan automáticamente por las reservas
     */
    public RestaurantTable updateStatus(Long id, RestaurantTable.TableStatus status) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        // Solo permitir cambios manuales a OCCUPIED o AVAILABLE
        // RESERVED se maneja automáticamente por el sistema de reservas
        if (status == RestaurantTable.TableStatus.OCCUPIED ||
                status == RestaurantTable.TableStatus.AVAILABLE) {
            table.setStatus(status);
            return tableRepository.save(table);
        } else {
            throw new RuntimeException("No se puede establecer manualmente el estado RESERVED. Se maneja automáticamente por las reservas.");
        }
    }

    /**
     * Elimina una mesa y reorganiza los IDs para mantener la secuencia
     */
    @Transactional
    public void delete(Long id) {
        // Verificar que la mesa existe
        RestaurantTable tableToDelete = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        // Verificar si la mesa tiene reservas activas FUTURAS
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> futureActiveReservations = reservationRepository
                .findActiveReservationsInTimeRange(now, now.plusDays(365))
                .stream()
                .filter(reservation -> reservation.getTable().getId().equals(id))
                .collect(Collectors.toList());

        if (!futureActiveReservations.isEmpty()) {
            throw new RuntimeException("No se puede eliminar la mesa porque tiene reservas activas futuras");
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
     * Verifica si una mesa tiene reservas activas futuras
     */
    public boolean hasActiveReservations(Long tableId) {
        LocalDateTime now = LocalDateTime.now();
        return reservationRepository.findActiveReservationsInTimeRange(now, now.plusDays(365))
                .stream()
                .anyMatch(reservation -> reservation.getTable().getId().equals(tableId));
    }

    /**
     * Verifica si una mesa tiene reservas activas para una fecha específica
     */
    public boolean hasActiveReservationsForDate(Long tableId, LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().atTime(23, 59, 59);

        return reservationRepository.findActiveReservationsInTimeRange(startOfDay, endOfDay)
                .stream()
                .anyMatch(reservation -> reservation.getTable().getId().equals(tableId));
    }
}