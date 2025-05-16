package com.restaurant.controller;

import com.restaurant.model.User;
import com.restaurant.dto.UserDto;
import com.restaurant.repository.RoleRepository;
import com.restaurant.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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
        model.addAttribute("userDto", new UserDto());
        model.addAttribute("roles", roleRepository.findAll());
        return "user/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario en el sistema")
    public String createUser(
            @Valid @ModelAttribute("userDto") UserDto userDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes attributes) {

        // Validar si ya existe el nombre de usuario
        if (userService.existsByUsername(userDto.getUsername())) {
            bindingResult.rejectValue("username", "validation.username.exists", "Este nombre de usuario ya existe");
        }

        // Validar que las contraseñas coincidan
        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "validation.password.match", "Las contraseñas no coinciden");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleRepository.findAll());
            return "user/form";
        }

        userService.save(userDto);
        attributes.addFlashAttribute("successMessage", "Usuario creado correctamente");
        return "redirect:/users";
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
        userDto.setRole(user.getRoles().iterator().next().getName());

        model.addAttribute("userDto", userDto);
        model.addAttribute("roles", roleRepository.findAll());
        return "user/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente")
    public String updateUser(
            @Parameter(description = "ID del usuario a actualizar") @PathVariable Long id,
            @Valid @ModelAttribute("userDto") UserDto userDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes attributes) {

        // Validar que las contraseñas coincidan si se está cambiando
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty() &&
                !userDto.getPassword().equals(userDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "validation.password.match", "Las contraseñas no coinciden");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleRepository.findAll());
            return "user/form";
        }

        userDto.setId(id);
        userService.update(userDto);
        attributes.addFlashAttribute("successMessage", "Usuario actualizado correctamente");
        return "redirect:/users";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario del sistema")
    public String deleteUser(
            @Parameter(description = "ID del usuario a eliminar") @PathVariable Long id,
            RedirectAttributes attributes) {

        userService.delete(id);
        attributes.addFlashAttribute("successMessage", "Usuario eliminado correctamente");
        return "redirect:/users";
    }

    @GetMapping("/profile")
    @Operation(summary = "Ver perfil", description = "Muestra el perfil del usuario actual")
    public String showProfile(Model model) {
        // Obtener el usuario actual
        User currentUser = userService.getCurrentUser();

        // Crear el DTO y mapear los datos del usuario
        UserDto userDto = new UserDto();
        userDto.setId(currentUser.getId());
        userDto.setName(currentUser.getName());
        userDto.setLastName(currentUser.getLastName());
        userDto.setUsername(currentUser.getUsername());
        userDto.setPhone(currentUser.getPhone());

        // Añadir el usuario y el DTO al modelo
        model.addAttribute("user", currentUser);
        model.addAttribute("userDto", userDto);
        return "user/profile";
    }
}
