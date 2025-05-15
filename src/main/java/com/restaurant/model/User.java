package com.restaurant.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@Schema(description = "Modelo que representa a un usuario del sistema")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    @Schema(description = "Identificador único del usuario", example = "1")
    private Long id;

    @Column(name = "user_name", nullable = false)
    @Schema(description = "Nombre del usuario", example = "Juan")
    private String name;

    @Column(name = "user_last_name", nullable = false)
    @Schema(description = "Apellido del usuario", example = "Pérez")
    private String lastName;

    @Column(name = "user_username", nullable = false, unique = true)
    @Schema(description = "Nombre de usuario para acceso al sistema", example = "jperez")
    private String username;

    @Column(name = "user_password", nullable = false)
    @Schema(description = "Contraseña del usuario (encriptada)", example = "$2a$10$...")
    private String password;

    @Column(name = "user_phone")
    @Schema(description = "Número de teléfono del usuario", example = "1234567890")
    private String phone;

    @Column(name = "user_enabled")
    @Schema(description = "Indica si el usuario está activo en el sistema", example = "true")
    private Boolean enabled = true;

    @Column(name = "user_created_at", updatable = false)
    @Schema(description = "Fecha y hora de creación del usuario", example = "2025-01-01T12:00:00")
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Schema(description = "Roles asignados al usuario")
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}