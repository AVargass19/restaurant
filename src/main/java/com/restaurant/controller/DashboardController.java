package com.restaurant.controller;

import com.restaurant.model.Reservation;
import com.restaurant.model.ReservationHistory;
import com.restaurant.model.RestaurantTable;
import com.restaurant.model.User;
import com.restaurant.service.ReservationHistoryService;
import com.restaurant.service.ReservationService;
import com.restaurant.service.RestaurantTableService;
import com.restaurant.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Tag(name = "Dashboard Controller", description = "Controlador para el panel principal del sistema")
public class DashboardController {

    private final UserService userService;
    private final ReservationService reservationService;
    private final RestaurantTableService restaurantTableService;
    private final ReservationHistoryService historyService;

    @Autowired
    public DashboardController(
            UserService userService,
            ReservationService reservationService,
            RestaurantTableService restaurantTableService,
            ReservationHistoryService historyService) {
        this.userService = userService;
        this.reservationService = reservationService;
        this.restaurantTableService = restaurantTableService;
        this.historyService = historyService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard", description = "Muestra el panel principal según el rol del usuario")
    public String dashboard(Model model) {
        // Obtener el usuario actual
        User currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);

        // Verificar los roles del usuario
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        boolean isStaff = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_STAFF"));
        boolean isUser = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_USER"));

        // Añadir flags de rol al modelo
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isStaff", isStaff);
        model.addAttribute("isUser", isUser);

        // Si es ADMIN
        if (isAdmin) {
            // Estadísticas para el dashboard de admin
            model.addAttribute("totalUsers", userService.countAll());
            model.addAttribute("totalTables", restaurantTableService.findAll().size());
            model.addAttribute("activeReservations", reservationService.countActiveReservations());
            model.addAttribute("todayReservations", reservationService.countTodayReservations());

            // Acciones recientes del sistema
            List<ReservationHistory> allActions = historyService.findAll();

            // Ordenar todas las acciones por fecha de creación en orden descendente
            allActions.sort(Comparator.comparing(ReservationHistory::getCreatedAt).reversed());

            // Tomar las 10 más recientes
            List<ReservationHistory> recentActions = allActions.isEmpty() ? allActions :
                    (allActions.size() > 10 ? allActions.subList(0, 10) : allActions);

            model.addAttribute("recentActions", recentActions);

            // Añadir reservas recientes para el panel de gráfico
            // Ordenar las reservas: primero las futuras, luego las pasadas (en orden descendente)
            List<Reservation> reservations = reservationService.findAll();
            List<Reservation> sortedReservations = sortReservationsByDateFutureFirst(reservations);
            List<Reservation> recentReservations = sortedReservations.size() > 10 ?
                    sortedReservations.subList(0, 10) : sortedReservations;

            model.addAttribute("recentReservations", recentReservations);

            // IMPORTANTE: Estado dinámico de mesas para HOY
            List<RestaurantTable> tables = restaurantTableService.getTablesWithDynamicStatus(LocalDateTime.now());
            model.addAttribute("tables", tables);
        }

        // Si es STAFF
        if (isStaff || isAdmin) {
            // IMPORTANTE: Estado dinámico de mesas para HOY - Solo para staff si no es admin
            if (!isAdmin) {
                List<RestaurantTable> tables = restaurantTableService.getTablesWithDynamicStatus(LocalDateTime.now());
                model.addAttribute("tables", tables);
            }

            // Reservas del día - SOLO ACTIVAS y ordenadas por hora
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

            List<Reservation> todayReservations = reservationService.findByStatus(Reservation.ReservationStatus.ACTIVE)
                    .stream()
                    .filter(r -> r.getDate().isAfter(startOfDay) && r.getDate().isBefore(endOfDay))
                    .sorted((r1, r2) -> r1.getDate().compareTo(r2.getDate())) // Ordenar por hora ascendente
                    .collect(Collectors.toList());

            model.addAttribute("todayReservations", todayReservations);
        }

        // Si es USUARIO (o cualquier otro rol - todos deben ver sus reservas)
        if (isUser || isAdmin || isStaff) {
            // Reservas del usuario - ordenadas primero futuras, luego pasadas
            List<Reservation> userReservations = reservationService.findByUser(currentUser);
            userReservations = sortReservationsByDateFutureFirst(userReservations);
            model.addAttribute("userReservations", userReservations);
        }

        return "dashboard";
    }

    /**
     * Ordena las reservas: primero las futuras (ascendente), luego las pasadas (descendente)
     */
    private List<Reservation> sortReservationsByDateFutureFirst(List<Reservation> reservations) {
        LocalDateTime now = LocalDateTime.now();

        // Dividir en futuras y pasadas
        List<Reservation> futureReservations = reservations.stream()
                .filter(r -> r.getDate().isAfter(now))
                .sorted((r1, r2) -> r1.getDate().compareTo(r2.getDate())) // Ascendente para futuras
                .collect(Collectors.toList());

        List<Reservation> pastReservations = reservations.stream()
                .filter(r -> r.getDate().isBefore(now) || r.getDate().isEqual(now))
                .sorted((r1, r2) -> r2.getDate().compareTo(r1.getDate())) // Descendente para pasadas
                .collect(Collectors.toList());

        // Combinar las listas: primero futuras, luego pasadas
        List<Reservation> sortedReservations = new ArrayList<>(futureReservations);
        sortedReservations.addAll(pastReservations);

        return sortedReservations;
    }
}