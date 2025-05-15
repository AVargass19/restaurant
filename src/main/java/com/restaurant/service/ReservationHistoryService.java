package com.restaurant.service;

import com.restaurant.model.ReservationHistory;
import com.restaurant.repository.ReservationHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationHistoryService {

    private final ReservationHistoryRepository historyRepository;

    public ReservationHistoryService(ReservationHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public List<ReservationHistory> findAll() {
        return historyRepository.findAll();
    }

    public Optional<ReservationHistory> findById(Long id) {
        return historyRepository.findById(id);
    }

    public List<ReservationHistory> findByReservationId(Long reservationId) {
        return historyRepository.findByReservationIdOrderByCreatedAtDesc(reservationId);
    }

    public List<ReservationHistory> findByUserId(Long userId) {
        return historyRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<ReservationHistory> findByAction(ReservationHistory.ActionType action) {
        return historyRepository.findByActionOrderByCreatedAtDesc(action);
    }
}