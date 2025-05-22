package com.restaurant.controller;

import com.restaurant.model.User;
import com.restaurant.dto.UserDto;
import com.restaurant.repository.RoleRepository;
import com.restaurant.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/users")
@Tag(name = "User Controller", description = "Controlador para la gestión de usuarios")
public class UserController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    public UserController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Listar todos los usuarios", description = "Muestra la lista de usuarios registrados")
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "user/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Mostrar formulario de creación", description = "Formulario para crear un nuevo usuario")
    public String showCreateForm(Model model) {

        UserDto userDto = new UserDto();
        userDto.setEnabled(true);

        model.addAttribute("user", userDto);
        model.addAttribute("roles", roleRepository.findAll());
        return "user/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario en el sistema")
    public String createUser(
            @Valid @ModelAttribute("user") UserDto userDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes attributes) {

        // Validar si ya existe el nombre de usuario
        if (userService.existsByUsername(userDto.getUsername())) {
            bindingResult.rejectValue("username", "validation.username.exists", "Nombre de usuario ya existe");
        }

        // Validar que las contraseñas coincidan
        if (userDto.getPassword() != null && userDto.getConfirmPassword() != null &&
                !userDto.getPassword().equals(userDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "validation.password.match", "Las contraseñas no coinciden");
        }

        // Validar longitud mínima de contraseña
        if (userDto.getPassword() == null || userDto.getPassword().length() < 6) {
            bindingResult.rejectValue("password", "validation.password.length", "La contraseña debe tener al menos 6 caracteres");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userDto);
            model.addAttribute("roles", roleRepository.findAll());
            return "user/form";
        }

        try {
            userService.save(userDto);
            attributes.addFlashAttribute("successMessage", "Usuario creado correctamente");
            return "redirect:/users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al crear el usuario: " + e.getMessage());
            model.addAttribute("user", userDto);
            model.addAttribute("roles", roleRepository.findAll());
            return "user/form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Mostrar formulario de edición", description = "Formulario para editar un usuario existente")
    public String showEditForm(
            @Parameter(description = "ID del usuario a editar") @PathVariable Long id,
            Model model) {

        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setLastName(user.getLastName());
        userDto.setUsername(user.getUsername());
        userDto.setPhone(user.getPhone());
        userDto.setEnabled(user.getEnabled());


        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            userDto.setRole(user.getRoles().iterator().next().getName());
        }

        model.addAttribute("user", userDto);
        model.addAttribute("roles", roleRepository.findAll());
        return "user/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente")
    public String updateUser(
            @Parameter(description = "ID del usuario a actualizar") @PathVariable Long id,
            @Valid @ModelAttribute("user") UserDto userDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes attributes) {

        // Validar username solo si cambió
        User existingUser = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!existingUser.getUsername().equals(userDto.getUsername()) &&
                userService.existsByUsername(userDto.getUsername())) {
            bindingResult.rejectValue("username", "validation.username.exists", "Nombre de usuario ya existe");
        }

        // Validar contraseñas solo si se están cambiando
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            if (userDto.getPassword().length() < 6) {
                bindingResult.rejectValue("password", "validation.password.length", "La contraseña debe tener al menos 6 caracteres");
            }

            if (userDto.getConfirmPassword() != null &&
                    !userDto.getPassword().equals(userDto.getConfirmPassword())) {
                bindingResult.rejectValue("confirmPassword", "validation.password.match", "Las contraseñas no coinciden");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userDto);
            model.addAttribute("roles", roleRepository.findAll());
            return "user/form";
        }

        try {
            userDto.setId(id);
            userService.update(userDto);
            attributes.addFlashAttribute("successMessage", "Usuario actualizado correctamente");
            return "redirect:/users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al actualizar el usuario: " + e.getMessage());
            model.addAttribute("user", userDto);
            model.addAttribute("roles", roleRepository.findAll());
            return "user/form";
        }
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario del sistema")
    public String deleteUser(
            @Parameter(description = "ID del usuario a eliminar") @PathVariable Long id,
            RedirectAttributes attributes) {

        try {
            userService.delete(id);
            attributes.addFlashAttribute("successMessage", "Usuario eliminado correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("errorMessage", "Error al eliminar el usuario: " + e.getMessage());
        }

        return "redirect:/users";
    }

    // Endpoint para verificar disponibilidad de username
    @GetMapping("/api/check-username")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseBody
    @Operation(summary = "Verificar disponibilidad de username", description = "Verifica si un nombre de usuario está disponible")
    public ResponseEntity<Map<String, Boolean>> checkUsername(
            @Parameter(description = "Username a verificar") @RequestParam String username,
            @Parameter(description = "ID del usuario actual (opcional, para edición)") @RequestParam(required = false) Long userId) {

        Map<String, Boolean> response = new HashMap<>();

        try {
            boolean available;

            if (userId != null) {
                // Si estamos editando, verificar que no exista otro usuario con el mismo username
                User existingUser = userService.findById(userId).orElse(null);
                if (existingUser != null && existingUser.getUsername().equals(username)) {
                    // Es el mismo username del usuario actual
                    available = true;
                } else {
                    // Verificar si otro usuario tiene ese username
                    available = !userService.existsByUsername(username);
                }
            } else {
                available = !userService.existsByUsername(username);
            }

            response.put("available", available);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("available", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/profile")
    @Operation(summary = "Ver perfil", description = "Muestra el perfil del usuario actual")
    public String showProfile(Model model) {
        try {
            // Obtener el usuario actual
            User currentUser = userService.getCurrentUser();
            model.addAttribute("user", currentUser);
            return "user/profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar el perfil: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/profile/change-password")
    @Operation(summary = "Cambiar contraseña", description = "Permite al usuario cambiar su contraseña")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmNewPassword,
            Model model,
            RedirectAttributes attributes) {

        try {
            // Validar que las contraseñas nuevas coincidan
            if (!newPassword.equals(confirmNewPassword)) {
                model.addAttribute("errorMessage", "Las contraseñas nuevas no coinciden");
                model.addAttribute("user", userService.getCurrentUser());
                return "user/profile";
            }

            // Validar longitud mínima
            if (newPassword.length() < 6) {
                model.addAttribute("errorMessage", "La nueva contraseña debe tener al menos 6 caracteres");
                model.addAttribute("user", userService.getCurrentUser());
                return "user/profile";
            }

            // Cambiar la contraseña
            userService.changePassword(currentPassword, newPassword);

            attributes.addFlashAttribute("successMessage", "Contraseña cambiada correctamente");
            return "redirect:/users/profile";

        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("user", userService.getCurrentUser());
            return "user/profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cambiar la contraseña: " + e.getMessage());
            model.addAttribute("user", userService.getCurrentUser());
            return "user/profile";
        }
    }

    @GetMapping("/report")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Generar reporte de usuarios", description = "Genera un reporte PDF con todos los usuarios del sistema")
    public ResponseEntity<byte[]> generateUsersReport() {
        try {
            // Obtener todos los usuarios
            List<User> users = userService.findAll();

            // Generar el reporte usando JasperReports
            byte[] reportBytes = userService.generateUsersReport(users);

            // Configurar headers para la descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "users_report_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
            headers.setContentLength(reportBytes.length);

            return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            // Log del error
            System.err.println("Error generando reporte de usuarios: " + e.getMessage());
            e.printStackTrace();

            // Retornar error
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}