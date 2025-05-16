package com.restaurant.controller;

import com.restaurant.model.ReservationHistory;
import com.restaurant.service.ReservationHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/history")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name = "Reservation History Controller", description = "Controlador para la consulta del historial de reservas")
public class ReservationHistoryController {

    private final ReservationHistoryService historyService;

    public ReservationHistoryController(ReservationHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    @Operation(summary = "Ver historial completo", description = "Muestra todo el historial de cambios de reservas")
    public String viewHistory(Model model) {
        List<ReservationHistory> historyList = historyService.findAll();
        model.addAttribute("historyList", historyList);
        return "history/list";
    }

    @GetMapping("/reservation/{id}")
    @Operation(summary = "Ver historial por reserva", description = "Muestra el historial de cambios de una reserva específica")
    public String viewHistoryByReservation(@PathVariable Long id, Model model) {
        List<ReservationHistory> historyList = historyService.findByReservationId(id);
        model.addAttribute("historyList", historyList);
        return "history/list";
    }

    @GetMapping("/user/{id}")
    @Operation(summary = "Ver historial por usuario", description = "Muestra el historial de cambios realizados por un usuario específico")
    public String viewHistoryByUser(@PathVariable Long id, Model model) {
        List<ReservationHistory> historyList = historyService.findByUserId(id);
        model.addAttribute("historyList", historyList);
        return "history/list";
    }

    @GetMapping("/action/{action}")
    @Operation(summary = "Ver historial por tipo de acción", description = "Muestra el historial de cambios de un tipo específico (CREATE, UPDATE, DELETE)")
    public String viewHistoryByAction(@PathVariable ReservationHistory.ActionType action, Model model) {
        List<ReservationHistory> historyList = historyService.findByAction(action);
        model.addAttribute("historyList", historyList);
        return "history/list";
    }
}
