package com.restaurant.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
@Schema(description = "Modelo que representa los roles de usuario en el sistema")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    @Schema(description = "Identificador único del rol", example = "1")
    private Long id;

    @Column(name = "role_name", nullable = false, unique = true)
    @Schema(description = "Nombre del rol", example = "ROLE_ADMIN")
    private String name;
}