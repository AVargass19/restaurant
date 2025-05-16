package com.restaurant.controller;

import com.restaurant.model.User;
import com.restaurant.dto.UserDto;
import com.restaurant.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Tag(name = "Auth Controller", description = "Controlador para la autenticación de usuarios")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    @Operation(summary = "Página de inicio", description = "Muestra la página principal del sistema")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    @Operation(summary = "Página de login", description = "Muestra el formulario de inicio de sesión")
    public String showLoginForm() {
        return "auth/login";
    }

    @GetMapping("/register")
    @Operation(summary = "Página de registro", description = "Muestra el formulario de registro de usuario")
    public String showRegisterForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "auth/register";
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario", description = "Procesa el registro de un nuevo usuario")
    public String registerUser(
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
            return "auth/register";
        }

        // Asignar rol de usuario por defecto
        userDto.setRole("ROLE_USER");

        User user = userService.save(userDto);
        attributes.addFlashAttribute("successMessage", "Registro exitoso. Ahora puedes iniciar sesión.");
        return "redirect:/login";
    }

    @GetMapping("/access-denied")
    @Operation(summary = "Acceso denegado", description = "Página mostrada cuando se deniega el acceso")
    public String accessDenied() {
        return "error/access-denied";
    }

    @GetMapping("/menu")
    public String menu() {
        return "menu";
    }

    @GetMapping("/about-us")
    public String aboutUs() {
        return "about-us";
    }
}
