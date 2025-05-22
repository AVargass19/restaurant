package com.restaurant.controller;

import com.restaurant.model.ReservationHistory;
import com.restaurant.model.User;
import com.restaurant.service.ReservationHistoryService;
import com.restaurant.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/history")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name = "Reservation History Controller", description = "Controlador para la consulta del historial de reservas")
public class ReservationHistoryController {

    private final ReservationHistoryService historyService;
    private final UserService userService;

    public ReservationHistoryController(ReservationHistoryService historyService, UserService userService) {
        this.historyService = historyService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Ver historial completo", description = "Muestra todo el historial de cambios de reservas")
    public String viewHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {

        // Obtener el usuario actual para verificar permisos
        User currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);

        // Convertir la página a base 0 para Spring Data
        page = page <= 0 ? 0 : page - 1;

        // Obtener todos los registros de historial
        List<ReservationHistory> allHistory = historyService.findAll();

        // Aplicar filtros si existen
        if (query != null && !query.trim().isEmpty()) {
            String searchTerm = query.toLowerCase();
            allHistory = allHistory.stream()
                    .filter(h ->
                            (h.getUser() != null &&
                                    (h.getUser().getName().toLowerCase().contains(searchTerm) ||
                                            h.getUser().getLastName().toLowerCase().contains(searchTerm))) ||
                                    (h.getReservation() != null &&
                                            h.getReservation().getId().toString().contains(searchTerm)) ||
                                    h.getAction().name().toLowerCase().contains(searchTerm) ||
                                    h.getId().toString().contains(searchTerm))
                    .collect(Collectors.toList());
        }

        // Filtrar por fechas si se proporcionan
        if (startDate != null && !startDate.isEmpty()) {
            LocalDateTime startDateTime = LocalDate.parse(startDate).atStartOfDay();
            allHistory = allHistory.stream()
                    .filter(h -> h.getCreatedAt().isAfter(startDateTime) || h.getCreatedAt().isEqual(startDateTime))
                    .collect(Collectors.toList());
        }

        if (endDate != null && !endDate.isEmpty()) {
            LocalDateTime endDateTime = LocalDate.parse(endDate).atTime(LocalTime.MAX);
            allHistory = allHistory.stream()
                    .filter(h -> h.getCreatedAt().isBefore(endDateTime) || h.getCreatedAt().isEqual(endDateTime))
                    .collect(Collectors.toList());
        }

        // Crear paginación
        int totalElements = allHistory.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // Verificar que la página solicitada esté en rango
        if (page >= totalPages) {
            page = totalPages > 0 ? totalPages - 1 : 0;
        }

        // Obtener subconjunto para la página actual
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<ReservationHistory> pageContent = fromIndex < toIndex
                ? allHistory.subList(fromIndex, toIndex)
                : List.of();

        // Agregar atributos al modelo
        model.addAttribute("historyList", pageContent);
        model.addAttribute("currentPage", page + 1);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalElements", totalElements);

        return "history/list";
    }

    @GetMapping("/reservation/{id}")
    @Operation(summary = "Ver historial por reserva", description = "Muestra el historial de cambios de una reserva específica")
    public String viewHistoryByReservation(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        // Obtener el usuario actual
        User currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);

        // Obtener historial filtrado por reserva
        List<ReservationHistory> historyList = historyService.findByReservationId(id);

        // Crear paginación
        int totalElements = historyList.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        page = page <= 0 ? 0 : page - 1;

        if (page >= totalPages) {
            page = totalPages > 0 ? totalPages - 1 : 0;
        }

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<ReservationHistory> pageContent = fromIndex < toIndex
                ? historyList.subList(fromIndex, toIndex)
                : List.of();

        model.addAttribute("historyList", pageContent);
        model.addAttribute("currentPage", page + 1);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalElements", totalElements);

        return "history/list";
    }

    @GetMapping("/user/{id}")
    @Operation(summary = "Ver historial por usuario", description = "Muestra el historial de cambios realizados por un usuario específico")
    public String viewHistoryByUser(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        // Obtener el usuario actual
        User currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);

        // Obtener historial filtrado por usuario
        List<ReservationHistory> historyList = historyService.findByUserId(id);

        // Crear paginación
        int totalElements = historyList.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        page = page <= 0 ? 0 : page - 1;

        if (page >= totalPages) {
            page = totalPages > 0 ? totalPages - 1 : 0;
        }

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<ReservationHistory> pageContent = fromIndex < toIndex
                ? historyList.subList(fromIndex, toIndex)
                : List.of();

        model.addAttribute("historyList", pageContent);
        model.addAttribute("currentPage", page + 1);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalElements", totalElements);

        return "history/list";
    }

    @GetMapping("/action/{action}")
    @Operation(summary = "Ver historial por tipo de acción", description = "Muestra el historial de cambios de un tipo específico (CREATE, UPDATE, DELETE)")
    public String viewHistoryByAction(
            @PathVariable ReservationHistory.ActionType action,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        // Obtener el usuario actual
        User currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);

        // Obtener historial filtrado por tipo de acción
        List<ReservationHistory> historyList = historyService.findByAction(action);

        // Crear paginación
        int totalElements = historyList.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        page = page <= 0 ? 0 : page - 1;

        if (page >= totalPages) {
            page = totalPages > 0 ? totalPages - 1 : 0;
        }

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<ReservationHistory> pageContent = fromIndex < toIndex
                ? historyList.subList(fromIndex, toIndex)
                : List.of();

        model.addAttribute("historyList", pageContent);
        model.addAttribute("currentPage", page + 1);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalElements", totalElements);

        return "history/list";
    }
}