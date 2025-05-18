package com.restaurant.repository;

import com.restaurant.model.Reservation;
import com.restaurant.model.RestaurantTable;
import com.restaurant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUser(User user);

    List<Reservation> findByUserAndStatus(User user, Reservation.ReservationStatus status);

    List<Reservation> findByStatus(Reservation.ReservationStatus status);

    // Método modificado: ahora toma directamente rango de fechas y estado
    @Query("SELECT r FROM Reservation r WHERE r.table.id = :tableId AND r.date BETWEEN :startTime AND :endTime AND r.status = :status")
    List<Reservation> findByTableAndDateBetweenAndStatus(
            @Param("tableId") Long tableId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("status") Reservation.ReservationStatus status);

    // Nuevo método para encontrar todas las reservas activas dentro de un rango de tiempo
    @Query("SELECT r FROM Reservation r WHERE r.date BETWEEN :startTime AND :endTime AND r.status = 'ACTIVE'")
    List<Reservation> findActiveReservationsInTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}