package com.restaurant.service;

import com.restaurant.model.ReservationHistory;
import com.restaurant.repository.ReservationHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationHistoryService {

    private final ReservationHistoryRepository historyRepository;

    public ReservationHistoryService(ReservationHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    /**
     * Encuentra todos los registros de historial ordenados por fecha de creación descendente (más recientes primero)
     * @return Lista de registros de historial
     */
    public List<ReservationHistory> findAll() {
        List<ReservationHistory> historyList = historyRepository.findAll();
        return historyList.stream()
                .sorted(Comparator.comparing(ReservationHistory::getCreatedAt).reversed())
                .collect(Collectors.toList());
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

    /**
     * Encuentra los N registros de historial más recientes
     * @param limit Cantidad máxima de registros a devolver
     * @return Lista de los N registros más recientes
     */
    public List<ReservationHistory> findMostRecent(int limit) {
        List<ReservationHistory> allHistory = findAll();
        return allHistory.size() > limit ? allHistory.subList(0, limit) : allHistory;
    }
}