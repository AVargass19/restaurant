package com.restaurant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para crear o actualizar un usuario")
public class UserDto {

    @Schema(description = "Identificador único del usuario", example = "1")
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nombre del usuario", example = "Juan")
    private String name;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    @Schema(description = "Apellido del usuario", example = "Pérez")
    private String lastName;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 4, max = 50, message = "El nombre de usuario debe tener entre 4 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]{4,50}$", message = "El nombre de usuario solo puede contener letras, números, puntos, guiones y guiones bajos")
    @Schema(description = "Nombre de usuario para acceso al sistema", example = "jperez")
    private String username;

    @Schema(description = "Contraseña del usuario", example = "password123")
    private String password;

    @Schema(description = "Confirmación de la contraseña", example = "password123")
    private String confirmPassword;

    @Pattern(regexp = "^[0-9]{7,20}$", message = "El teléfono solo puede contener números y debe tener entre 7 y 20 dígitos")
    @Schema(description = "Número de teléfono del usuario", example = "1234567890")
    private String phone;

    @Schema(description = "Indica si el usuario está activo", example = "true")
    private Boolean enabled = true;

    @Schema(description = "Rol del usuario (ROLE_ADMIN, ROLE_USER, ROLE_STAFF)", example = "ROLE_USER")
    private String role;
}