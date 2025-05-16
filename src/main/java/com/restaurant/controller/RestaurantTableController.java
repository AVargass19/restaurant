package com.restaurant.controller;

import com.restaurant.model.RestaurantTable;
import com.restaurant.service.RestaurantTableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/tables")
@Tag(name = "Restaurant Table Controller", description = "Controlador para la gestión de mesas del restaurante")
public class RestaurantTableController {

    private final RestaurantTableService tableService;

    public RestaurantTableController(RestaurantTableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    @Operation(summary = "Listar todas las mesas", description = "Muestra la lista de mesas del restaurante")
    public String listTables(Model model) {
        List<RestaurantTable> tables = tableService.findAll();
        model.addAttribute("tables", tables);
        return "table/list";
    }

    @GetMapping("/available")
    @Operation(summary = "Listar mesas disponibles", description = "Muestra la lista de mesas disponibles")
    public String listAvailableTables(Model model) {
        List<RestaurantTable> tables = tableService.findByStatus(RestaurantTable.TableStatus.AVAILABLE);
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
    public String createTable(@ModelAttribute RestaurantTable table, RedirectAttributes attributes) {
        tableService.save(table);
        attributes.addFlashAttribute("successMessage", "Mesa creada correctamente");
        return "redirect:/tables";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STAFF')")
    @Operation(summary = "Mostrar formulario de edición", description = "Formulario para editar una mesa existente")
    public String showEditForm(
            @Parameter(description = "ID de la mesa a editar") @PathVariable Long id,
            Model model) {

        RestaurantTable table = tableService.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        model.addAttribute("table", table);
        model.addAttribute("statuses", RestaurantTable.TableStatus.values());
        return "table/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STAFF')")
    @Operation(summary = "Actualizar mesa", description = "Actualiza los datos de una mesa existente")
    public String updateTable(
            @Parameter(description = "ID de la mesa a actualizar") @PathVariable Long id,
            @ModelAttribute RestaurantTable table,
            RedirectAttributes attributes) {

        table.setId(id);
        tableService.save(table);
        attributes.addFlashAttribute("successMessage", "Mesa actualizada correctamente");
        return "redirect:/tables";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Eliminar mesa", description = "Elimina una mesa del sistema")
    public String deleteTable(
            @Parameter(description = "ID de la mesa a eliminar") @PathVariable Long id,
            RedirectAttributes attributes) {

        tableService.delete(id);
        attributes.addFlashAttribute("successMessage", "Mesa eliminada correctamente");
        return "redirect:/tables";
    }

    @GetMapping("/status/{id}/{status}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STAFF')")
    @Operation(summary = "Cambiar estado de mesa", description = "Actualiza el estado de una mesa")
    public String updateTableStatus(
            @Parameter(description = "ID de la mesa") @PathVariable Long id,
            @Parameter(description = "Nuevo estado de la mesa") @PathVariable RestaurantTable.TableStatus status,
            RedirectAttributes attributes) {

        tableService.updateStatus(id, status);
        attributes.addFlashAttribute("successMessage", "Estado de mesa actualizado correctamente");
        return "redirect:/tables";
    }
}
