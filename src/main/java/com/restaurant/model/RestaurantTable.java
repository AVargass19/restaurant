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
@Table(name = "restaurant_table")
@Schema(description = "Modelo que representa una mesa del restaurante")
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    @Schema(description = "Identificador único de la mesa", example = "1")
    private Long id;

    @Column(name = "table_status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Estado actual de la mesa", example = "AVAILABLE")
    private TableStatus status;

    public enum TableStatus {
        AVAILABLE,
        OCCUPIED,
        RESERVED
    }
}