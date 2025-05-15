package com.restaurant.repository;

import com.restaurant.model.Reservation;
import com.restaurant.model.RestaurantTable;
import com.restaurant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUser(User user);

    List<Reservation> findByUserAndStatus(User user, Reservation.ReservationStatus status);

    List<Reservation> findByStatus(Reservation.ReservationStatus status);

    List<Reservation> findByTable(RestaurantTable table);

    @Query("SELECT r FROM Reservation r WHERE r.date BETWEEN ?1 AND ?2 AND r.status = ?3")
    List<Reservation> findByDateBetweenAndStatus(LocalDateTime start, LocalDateTime end, Reservation.ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.table.id = ?1 AND r.date BETWEEN ?2 AND ?3 AND r.status = ?4")
    List<Reservation> findByTableAndDateBetweenAndStatus(Long tableId, LocalDateTime start, LocalDateTime end, Reservation.ReservationStatus status);
}
