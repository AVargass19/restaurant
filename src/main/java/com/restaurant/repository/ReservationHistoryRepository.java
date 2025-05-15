package com.restaurant.repository;

import com.restaurant.model.Reservation;
import com.restaurant.model.ReservationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationHistoryRepository extends JpaRepository<ReservationHistory, Long> {

    List<ReservationHistory> findByReservationOrderByCreatedAtDesc(Reservation reservation);

    List<ReservationHistory> findByReservationIdOrderByCreatedAtDesc(Long reservationId);

    List<ReservationHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ReservationHistory> findByActionOrderByCreatedAtDesc(ReservationHistory.ActionType action);
}
