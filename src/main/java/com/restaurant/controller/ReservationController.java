package com.restaurant.controller;

import com.restaurant.model.Reservation;
import com.restaurant.model.RestaurantTable;
import com.restaurant.model.User;
import com.restaurant.dto.ReservationDto;
import com.restaurant.repository.UserRepository;
import com.restaurant.security.CustomUserDetails;
import com.restaurant.service.ReservationService;
import com.restaurant.service.RestaurantTableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/reservations")
@Tag(name = "Reservation Controller", description = "Controlador para la gestión de reservas")
public class ReservationController {

    private final ReservationService reservationService;
    private final RestaurantTableService tableService;
    private final UserRepository userRepository;

    public ReservationController(
            ReservationService reservationService,
            RestaurantTableService tableService,
            UserRepository userRepository) {
        this.reservationService = reservationService;
        this.tableService = tableService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(summary = "Listar reservas", description = "Muestra las reservas según el rol del usuario")
    public String listReservations(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        List<Reservation> reservations;
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isStaff = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));

        if (isAdmin || isStaff) {
            // Admins y staff ven todas las reservas
            reservations = reservationService.findAll();
        } else {
            // Usuarios normales solo ven sus propias reservas
            User user = userRepository.findById(userDetails.getId()).orElseThrow();
            reservations = reservationService.findByUser(user);
        }

        model.addAttribute("reservations", reservations);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isStaff", isStaff);
        return "reservation/list";
    }

    @GetMapping("/create")
    @Operation(summary = "Mostrar formulario de creación", description = "Formulario para crear una nueva reserva")
    public String showCreateForm(Model model) {
        model.addAttribute("reservationDto", new ReservationDto());
        model.addAttribute("availableTables", tableService.findByStatus(RestaurantTable.TableStatus.AVAILABLE));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            model.addAttribute("users", userRepository.findAll());
        }

        return "reservation/form";
    }

    @PostMapping("/create")
    @Operation(summary = "Crear reserva", description = "Crea una nueva reserva en el sistema")
    public String createReservation(
            @Valid @ModelAttribute("reservationDto") ReservationDto reservationDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes attributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("availableTables", tableService.findByStatus(RestaurantTable.TableStatus.AVAILABLE));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (isAdmin) {
                model.addAttribute("users", userRepository.findAll());
            }

            return "reservation/form";
        }

        try {
            reservationService.create(reservationDto);
            attributes.addFlashAttribute("successMessage", "Reserva creada correctamente");
            return "redirect:/reservations";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("availableTables", tableService.findByStatus(RestaurantTable.TableStatus.AVAILABLE));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (isAdmin) {
                model.addAttribute("users", userRepository.findAll());
            }

            return "reservation/form";
        }
    }

    @GetMapping("/edit/{id}")
    @Operation(summary = "Mostrar formulario de edición", description = "Formulario para editar una reserva existente")
    public String showEditForm(
            @Parameter(description = "ID de la reserva a editar") @PathVariable Long id,
            Model model) {

        Reservation reservation = reservationService.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        // Verificar permisos
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isStaff = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));

        if (!isAdmin && !isStaff && !reservation.getUser().getId().equals(userDetails.getId())) {
            return "redirect:/error?message=No tienes permiso para editar esta reserva";
        }

        ReservationDto reservationDto = new ReservationDto();
        reservationDto.setId(reservation.getId());
        reservationDto.setUserId(reservation.getUser().getId());
        reservationDto.setTableId(reservation.getTable().getId());
        reservationDto.setDate(reservation.getDate());
        reservationDto.setGuests(reservation.getGuests());
        reservationDto.setStatus(reservation.getStatus());

        model.addAttribute("reservationDto", reservationDto);

        // Incluir la mesa actual y las disponibles
        List<RestaurantTable> availableTables = tableService.findByStatus(RestaurantTable.TableStatus.AVAILABLE);
        if (reservation.getTable().getStatus() != RestaurantTable.TableStatus.AVAILABLE) {
            availableTables.add(reservation.getTable());
        }

        model.addAttribute("availableTables", availableTables);
        model.addAttribute("statuses", Reservation.ReservationStatus.values());

        if (isAdmin) {
            model.addAttribute("users", userRepository.findAll());
        }

        return "reservation/form";
    }

    @PostMapping("/edit/{id}")
    @Operation(summary = "Actualizar reserva", description = "Actualiza una reserva existente")
    public String updateReservation(
            @Parameter(description = "ID de la reserva a actualizar") @PathVariable Long id,
            @Valid @ModelAttribute("reservationDto") ReservationDto reservationDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes attributes) {

        if (bindingResult.hasErrors()) {
            List<RestaurantTable> availableTables = tableService.findByStatus(RestaurantTable.TableStatus.AVAILABLE);
            Reservation reservation = reservationService.findById(id).orElseThrow();

            if (reservation.getTable().getStatus() != RestaurantTable.TableStatus.AVAILABLE) {
                availableTables.add(reservation.getTable());
            }

            model.addAttribute("availableTables", availableTables);
            model.addAttribute("statuses", Reservation.ReservationStatus.values());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (isAdmin) {
                model.addAttribute("users", userRepository.findAll());
            }

            return "reservation/form";
        }

        try {
            reservationDto.setId(id);
            reservationService.update(reservationDto);
            attributes.addFlashAttribute("successMessage", "Reserva actualizada correctamente");
            return "redirect:/reservations";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());

            List<RestaurantTable> availableTables = tableService.findByStatus(RestaurantTable.TableStatus.AVAILABLE);
            Reservation reservation = reservationService.findById(id).orElseThrow();

            if (reservation.getTable().getStatus() != RestaurantTable.TableStatus.AVAILABLE) {
                availableTables.add(reservation.getTable());
            }

            model.addAttribute("availableTables", availableTables);
            model.addAttribute("statuses", Reservation.ReservationStatus.values());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (isAdmin) {
                model.addAttribute("users", userRepository.findAll());
            }

            return "reservation/form";
        }
    }

    @GetMapping("/cancel/{id}")
    @Operation(summary = "Cancelar reserva", description = "Cancela una reserva existente")
    public String cancelReservation(
            @Parameter(description = "ID de la reserva a cancelar") @PathVariable Long id,
            RedirectAttributes attributes) {

        try {
            reservationService.cancel(id);
            attributes.addFlashAttribute("successMessage", "Reserva cancelada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/reservations";
    }

    @GetMapping("/complete/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STAFF')")
    @Operation(summary = "Completar reserva", description = "Marca una reserva como completada")
    public String completeReservation(
            @Parameter(description = "ID de la reserva a completar") @PathVariable Long id,
            RedirectAttributes attributes) {

        try {
            reservationService.complete(id);
            attributes.addFlashAttribute("successMessage", "Reserva completada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/reservations";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Eliminar reserva", description = "Elimina una reserva del sistema")
    public String deleteReservation(
            @Parameter(description = "ID de la reserva a eliminar") @PathVariable Long id,
            RedirectAttributes attributes) {

        try {
            reservationService.delete(id);
            attributes.addFlashAttribute("successMessage", "Reserva eliminada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/reservations";
    }
}