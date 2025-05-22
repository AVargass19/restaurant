package com.restaurant.controller;

import com.restaurant.model.Reservation;
import com.restaurant.model.RestaurantTable;
import com.restaurant.repository.ReservationRepository;
import com.restaurant.service.RestaurantTableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.MessageSource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/tables")
@Tag(name = "Restaurant Table Controller", description = "Controlador para la gestión de mesas del restaurante")
public class RestaurantTableController {

    private final RestaurantTableService tableService;
    private final ReservationRepository reservationRepository;
    private final MessageSource messageSource;

    public RestaurantTableController(RestaurantTableService tableService,
                                     ReservationRepository reservationRepository,
                                     MessageSource messageSource) {
        this.tableService = tableService;
        this.reservationRepository = reservationRepository;
        this.messageSource = messageSource;
    }

    @GetMapping
    @Operation(summary = "Listar todas las mesas", description = "Muestra la lista de mesas del restaurante")
    public String listTables(Model model) {
        // Obtener todas las mesas y ordenarlas por ID
        List<RestaurantTable> tables = tableService.findAll();
        tables.sort(Comparator.comparing(RestaurantTable::getId));

        model.addAttribute("tables", tables);
        return "table/list";
    }

    @GetMapping("/available")
    @Operation(summary = "Listar mesas disponibles", description = "Muestra la lista de mesas disponibles")
    public String listAvailableTables(Model model) {
        // Obtener las mesas disponibles y ordenarlas por ID
        List<RestaurantTable> tables = tableService.findByStatus(RestaurantTable.TableStatus.AVAILABLE);
        tables.sort(Comparator.comparing(RestaurantTable::getId));

        model.addAttribute("tables", tables);
        return "table/available";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Mostrar formulario de creación", description = "Formulario para crear una nueva mesa")
    public String showCreateForm(Model model) {
        model.addAttribute("table", new RestaurantTable());
        model.addAttribute("statuses", RestaurantTable.TableStatus.values());
        return "table/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Crear mesa", description = "Crea una nueva mesa en el sistema")
    public String createTable(@ModelAttribute RestaurantTable table,
                              RedirectAttributes attributes,
                              Locale locale) {
        try {
            tableService.save(table);
            String message = messageSource.getMessage("table.create.success", null, locale);
            attributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            String message = messageSource.getMessage("table.create.error", null, locale);
            attributes.addFlashAttribute("errorMessage", message);
        }
        return "redirect:/tables";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STAFF')")
    @Operation(summary = "Mostrar formulario de edición", description = "Formulario para editar una mesa existente")
    public String showEditForm(
            @Parameter(description = "ID de la mesa a editar") @PathVariable Long id,
            Model model,
            Locale locale) {

        try {
            RestaurantTable table = tableService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

            model.addAttribute("table", table);
            model.addAttribute("statuses", RestaurantTable.TableStatus.values());
            return "table/form";
        } catch (Exception e) {
            String message = messageSource.getMessage("table.not.found", null, locale);
            model.addAttribute("errorMessage", message);
            return "redirect:/tables";
        }
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STAFF')")
    @Operation(summary = "Actualizar mesa", description = "Actualiza los datos de una mesa existente")
    public String updateTable(
            @Parameter(description = "ID de la mesa a actualizar") @PathVariable Long id,
            @ModelAttribute RestaurantTable table,
            RedirectAttributes attributes,
            Locale locale) {

        try {
            table.setId(id);
            tableService.save(table);
            String message = messageSource.getMessage("table.update.success", null, locale);
            attributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            String message = messageSource.getMessage("table.update.error", null, locale);
            attributes.addFlashAttribute("errorMessage", message);
        }
        return "redirect:/tables";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Eliminar mesa", description = "Elimina una mesa del sistema")
    public String deleteTable(
            @Parameter(description = "ID de la mesa a eliminar") @PathVariable Long id,
            RedirectAttributes attributes,
            Locale locale) {

        try {
            tableService.delete(id);
            String message = messageSource.getMessage("table.delete.success", null, locale);
            attributes.addFlashAttribute("successMessage", message);
        } catch (RuntimeException e) {
            String message;
            if (e.getMessage().contains("reservas activas")) {
                message = messageSource.getMessage("table.delete.error.active.reservations", null, locale);
            } else if (e.getMessage().contains("no encontrada")) {
                message = messageSource.getMessage("table.not.found", null, locale);
            } else {
                message = messageSource.getMessage("table.delete.error", null, locale);
            }
            attributes.addFlashAttribute("errorMessage", message);
        } catch (Exception e) {
            String message = messageSource.getMessage("table.delete.error", null, locale);
            attributes.addFlashAttribute("errorMessage", message);
        }
        return "redirect:/tables";
    }

    @GetMapping("/status/{id}/{status}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STAFF')")
    @Operation(summary = "Cambiar estado de mesa", description = "Actualiza el estado de una mesa")
    public String updateTableStatus(
            @Parameter(description = "ID de la mesa") @PathVariable Long id,
            @Parameter(description = "Nuevo estado de la mesa") @PathVariable RestaurantTable.TableStatus status,
            RedirectAttributes attributes,
            Locale locale) {

        try {
            tableService.updateStatus(id, status);
            String message = messageSource.getMessage("table.status.update.success", null, locale);
            attributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            String message = messageSource.getMessage("table.status.update.error", null, locale);
            attributes.addFlashAttribute("errorMessage", message);
        }
        return "redirect:/tables";
    }

    // Endpoint para obtener el estado de las mesas para una fecha específica
    @GetMapping("/api/tables/status")
    @ResponseBody
    public Map<String, String> getTablesStatusForDate(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        // Si no se proporciona fecha, usamos la fecha actual
        if (date == null) {
            date = LocalDate.now();
        }

        // Convertir la fecha a LocalDateTime (principio y fin del día)
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        // Obtener todas las mesas
        List<RestaurantTable> allTables = tableService.findAll();
        // Ordenar las mesas por ID
        allTables.sort(Comparator.comparing(RestaurantTable::getId));

        // Obtener reservas activas para el día
        List<Reservation> activeReservations = reservationRepository.findActiveReservationsInTimeRange(startOfDay, endOfDay);

        // Mapear IDs de mesas reservadas
        Set<Long> reservedTableIds = activeReservations.stream()
                .map(reservation -> reservation.getTable().getId())
                .collect(Collectors.toSet());

        // Crear mapa de resultado (tableId -> status)
        Map<String, String> result = new HashMap<>();

        for (RestaurantTable table : allTables) {
            String status;

            if (reservedTableIds.contains(table.getId())) {
                status = "RESERVED";
            } else {
                // IMPORTANTE: Ignoramos el estado persistente en la base de datos
                // y solo consideramos si tiene una reserva activa
                status = "AVAILABLE";
            }

            result.put(table.getId().toString(), status);
        }

        return result;
    }
}